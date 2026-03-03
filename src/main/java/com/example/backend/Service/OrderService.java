package com.example.backend.Service;

import com.example.backend.Model.*;
import com.example.backend.Repository.*;
import com.example.backend.dto.CreateOrderItem;
import com.example.backend.dto.CreateOrderRequest;
import com.example.backend.dto.PayOrderRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final OrderDetailRepository detailRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final CustomerRepository customerRepo;
    private final CafeTableRepository tableRepo;
    private final PaymentRepository paymentRepo;

    public OrderService(
            OrderRepository orderRepo,
            OrderDetailRepository detailRepo,
            ProductRepository productRepo,
            UserRepository userRepo,
            CustomerRepository customerRepo,
            CafeTableRepository tableRepo,
            PaymentRepository paymentRepo
    ) {
        this.orderRepo = orderRepo;
        this.detailRepo = detailRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.customerRepo = customerRepo;
        this.tableRepo = tableRepo;
        this.paymentRepo = paymentRepo;
    }

    // -------------------- READ --------------------
    public List<Order> getAllOrders() {
        return orderRepo.findAll(); // EntityGraph in repo will load orderDetails
    }

    public Order getOrderById(Integer id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    // -------------------- CREATE (SAVE ORDER) --------------------
    @Transactional
    public Order createOrder(CreateOrderRequest req) {

        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new RuntimeException("Order must have at least 1 item");
        }

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setOrderType(req.getOrderType() != null ? req.getOrderType() : "POS");
        order.setOrderStatus(req.getOrderStatus() != null ? req.getOrderStatus() : "SAVED");
        order.setTotalAmount(0.0);

        // user
        User user = userRepo.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + req.getUserId()));
        order.setUser(user);

        // customer (optional)
        if (req.getCustomerId() != null) {
            Customer customer = customerRepo.findById(req.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found: " + req.getCustomerId()));
            order.setCustomer(customer);
        }

        // table (optional)
        if (req.getTableId() != null) {
            CafeTable table = tableRepo.findById(req.getTableId())
                    .orElseThrow(() -> new RuntimeException("Table not found: " + req.getTableId()));
            order.setTable(table);
        }

        // receipt no
        String receiptNo = "R-" + System.currentTimeMillis();
        order.setReceiptNo(receiptNo);

        // save header first to get orderId
        Order savedOrder = orderRepo.save(order);

        double total = 0.0;

        // IMPORTANT: make sure list exists so JSON returns items
        if (savedOrder.getOrderDetails() == null) {
            savedOrder.setOrderDetails(new ArrayList<>());
        }

        for (CreateOrderItem item : req.getItems()) {

            Product product = productRepo.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

            int qty = item.getQuantity() == null ? 0 : item.getQuantity();
            if (qty <= 0) throw new RuntimeException("Quantity must be > 0");

            if (product.getStockQty() < qty) {
                // allow POS sale, but you can log it
                System.out.println("LOW STOCK: " + product.getProductName()
                        + " stock=" + product.getStockQty() + ", requested=" + qty);
            }

            double unitPrice = (item.getUnitPrice() != null) ? item.getUnitPrice() : product.getPrice();
            double subTotal = unitPrice * qty;
            total += subTotal;

            OrderDetail detail = new OrderDetail();
            detail.setOrder(savedOrder);
            detail.setProduct(product);
            detail.setQuantity(qty);
            detail.setUnitPrice(unitPrice);
            detail.setSubTotal(subTotal);

            savedOrder.getOrderDetails().add(detail);

            // reduce stock
            product.setStockQty(product.getStockQty() - qty);
            productRepo.save(product);
        }

        savedOrder.setTotalAmount(total);
        return orderRepo.save(savedOrder);
    }

    // -------------------- UPDATE (EDIT SAVED ORDER) --------------------
    @Transactional
    public Order updateOrder(Integer orderId, CreateOrderRequest req) {

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // lock after payment
        if ("PAID".equalsIgnoreCase(order.getOrderStatus()) || order.getPayment() != null) {
            throw new RuntimeException("Paid orders cannot be updated.");
        }

        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new RuntimeException("Order must have at least 1 item");
        }

        // 1) restore stock from old details
        List<OrderDetail> oldDetails = detailRepo.findByOrder_OrderId(orderId);
        for (OrderDetail d : oldDetails) {
            Product p = d.getProduct();
            p.setStockQty(p.getStockQty() + d.getQuantity());
            productRepo.save(p);
        }

        // 2) delete old details
        detailRepo.deleteAll(oldDetails);

        // 3) clear list so response doesn't show old items
        if (order.getOrderDetails() != null) {
            order.getOrderDetails().clear();
        } else {
            order.setOrderDetails(new ArrayList<>());
        }

        // 4) update optional fields
        if (req.getOrderType() != null) order.setOrderType(req.getOrderType());
        if (req.getOrderStatus() != null) order.setOrderStatus(req.getOrderStatus());

        if (req.getUserId() != null) {
            User user = userRepo.findById(req.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + req.getUserId()));
            order.setUser(user);
        }

        if (req.getCustomerId() != null) {
            Customer customer = customerRepo.findById(req.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found: " + req.getCustomerId()));
            order.setCustomer(customer);
        } else {
            order.setCustomer(null);
        }

        if (req.getTableId() != null) {
            CafeTable table = tableRepo.findById(req.getTableId())
                    .orElseThrow(() -> new RuntimeException("Table not found: " + req.getTableId()));
            order.setTable(table);
        } else {
            order.setTable(null);
        }

        // 5) create new details + reduce stock
        double total = 0.0;

        for (CreateOrderItem item : req.getItems()) {

            Product product = productRepo.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

            int qty = item.getQuantity() == null ? 0 : item.getQuantity();
            if (qty <= 0) throw new RuntimeException("Quantity must be > 0");

            if (product.getStockQty() < qty) {
                throw new RuntimeException("Not enough stock for: " + product.getProductName());
            }

            double unitPrice = (item.getUnitPrice() != null) ? item.getUnitPrice() : product.getPrice();
            double subTotal = unitPrice * qty;
            total += subTotal;

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQuantity(qty);
            detail.setUnitPrice(unitPrice);
            detail.setSubTotal(subTotal);

            OrderDetail savedDetail = detailRepo.save(detail);
            order.getOrderDetails().add(savedDetail);

            // reduce stock again
            product.setStockQty(product.getStockQty() - qty);
            productRepo.save(product);
        }

        order.setTotalAmount(total);
        return orderRepo.save(order);
    }

    // -------------------- PAY (MARK AS PAID) --------------------
    @Transactional
    public Order payOrder(Integer orderId, PayOrderRequest req) {

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // prevent duplicate payment
        if (order.getPayment() != null) {
            throw new RuntimeException("Order already has a payment.");
        }

        if ("PAID".equalsIgnoreCase(order.getOrderStatus())) {
            throw new RuntimeException("Order already PAID.");
        }

        BigDecimal total = BigDecimal.valueOf(order.getTotalAmount());

        BigDecimal paid = (req.getPaidAmount() != null) ? req.getPaidAmount() : total;

        if (paid.compareTo(total) < 0) {
            throw new RuntimeException("Paid amount cannot be less than total amount.");
        }

        BigDecimal change = paid.subtract(total);

        Payment payment = new Payment();
        payment.setPaymentType(req.getPaymentType());
        payment.setPaidAmount(paid);
        payment.setChangeAmount(change);
        payment.setPaymentDate(LocalDateTime.now());

        Payment savedPayment = paymentRepo.save(payment);

        order.setPayment(savedPayment);
        order.setOrderStatus("PAID");

        return orderRepo.save(order);
    }

    // -------------------- UPDATE STATUS --------------------
    @Transactional
    public Order updateStatus(Integer orderId, String status) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // lock if paid
        if ("PAID".equalsIgnoreCase(order.getOrderStatus())) {
            throw new RuntimeException("Paid orders cannot be modified.");
        }

        order.setOrderStatus(status);
        return orderRepo.save(order);
    }

    // -------------------- CANCEL (RESTORE STOCK + DELETE DETAILS + DELETE ORDER) --------------------
    @Transactional
    public void cancelOrder(Integer orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // lock if paid
        if ("PAID".equalsIgnoreCase(order.getOrderStatus())) {
            throw new RuntimeException("Paid orders cannot be cancelled.");
        }

        List<OrderDetail> details = detailRepo.findByOrder_OrderId(orderId);

        // restore stock
        for (OrderDetail d : details) {
            Product p = d.getProduct();
            p.setStockQty(p.getStockQty() + d.getQuantity());
            productRepo.save(p);
        }

        detailRepo.deleteAll(details);
        orderRepo.delete(order);
    }
}