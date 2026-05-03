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
    private final ProductIngredientRepository productIngredientRepo;
    private final IngredientRepository ingredientRepo;
    private final StockMovementRepository stockMovementRepo;
    private final TelegramService telegramService;
//    private final BakongService bakongService;

    public OrderService(
            OrderRepository orderRepo,
            OrderDetailRepository detailRepo,
            ProductRepository productRepo,
            UserRepository userRepo,
            CustomerRepository customerRepo,
            CafeTableRepository tableRepo,
            PaymentRepository paymentRepo,
            ProductIngredientRepository productIngredientRepo,
            IngredientRepository ingredientRepo,
            StockMovementRepository stockMovementRepo,
            TelegramService telegramService
    ) {
        this.orderRepo = orderRepo;
        this.detailRepo = detailRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.customerRepo = customerRepo;
        this.tableRepo = tableRepo;
        this.paymentRepo = paymentRepo;
        this.productIngredientRepo = productIngredientRepo;
        this.ingredientRepo = ingredientRepo;
        this.stockMovementRepo = stockMovementRepo;
        this.telegramService = telegramService;
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
        order.setOrderType(req.getOrderType() != null ? req.getOrderType() : "POS");
        order.setOrderStatus(req.getOrderStatus() != null ? req.getOrderStatus() : "SAVED");
        order.setOrderDate(LocalDateTime.now());
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

            Long qty = item.getQuantity() == null ? 0 : item.getQuantity();
            if (qty <= 0) throw new RuntimeException("Quantity must be > 0");

            if ("DIRECT".equalsIgnoreCase(product.getTrackMode())) {
                if (product.getStockQty() < qty) {
                    System.out.println("LOW STOCK: " + product.getProductName());
                }
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
            deductStock(product, qty, savedOrder.getOrderId());
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

            Long qty = item.getQuantity() == null ? 0 : item.getQuantity();
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
            deductStock(product, qty, order.getOrderId());
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
        telegramService.sendMessage(
                "✅ *Payment Successful*\n\n" +
                        "🧾 Order ID: #" + order.getOrderId() + "\n" +
                        "💰 Total: $" + order.getTotalAmount() + "\n" +
                        "💵 Paid: $" + paid + "\n" +
                        "🔄 Change: $" + change + "\n\n" +
                        "📅 " + LocalDateTime.now()
        );
        return orderRepo.save(order);
    }

    // -------------------- UPDATE STATUS --------------------
    @Transactional
    public Order updateStatus(Integer orderId, String status) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setOrderStatus(status);
        return orderRepo.save(order);
    }

    // -------------------- CANCEL (RESTORE STOCK + DELETE DETAILS + DELETE ORDER) --------------------
    @Transactional
    public void cancelOrder(Integer orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        List<OrderDetail> details = detailRepo.findByOrder_OrderId(orderId);

        for (OrderDetail d : details) {
            restoreStock(d.getProduct(), d.getQuantity());
        }

        detailRepo.deleteAll(details);
        orderRepo.delete(order);
    }
    private void deductStock(Product product, Long qty, Integer orderId) {

        // DIRECT stock items (bottled drinks etc)
        if ("DIRECT".equalsIgnoreCase(product.getTrackMode())) {

            if (product.getStockQty() < qty) {
                throw new RuntimeException("Not enough stock for " + product.getProductName());
            }

            product.setStockQty(product.getStockQty() - qty);
            productRepo.save(product);
            return;
        }

        // INGREDIENT based products (coffee drinks)
        List<ProductIngredient> recipe =
                productIngredientRepo.findByProduct_ProductId(product.getProductId());

        if (recipe.isEmpty()) {
            throw new RuntimeException("Recipe not defined for " + product.getProductName());
        }

        for (ProductIngredient r : recipe) {

            Ingredient ingredient = r.getIngredient();

            double requiredQty = r.getQtyRequired() * qty;

            if (ingredient.getStockQty() < requiredQty) {
                throw new RuntimeException(
                        "Not enough ingredient stock: " + ingredient.getIngredientName()
                );
            }

            // subtract ingredient stock
            ingredient.setStockQty(ingredient.getStockQty() - requiredQty);
            ingredientRepo.save(ingredient);

            // record movement
            StockMovement movement = new StockMovement();
            movement.setIngredient(ingredient);
            movement.setMovementType("OUT");
            movement.setQuantity(requiredQty);
            movement.setReferenceType("ORDER");
            movement.setReferenceId(orderId);
            movement.setNote("Used for product: " + product.getProductName());

            stockMovementRepo.save(movement);
        }
    }
    private void restoreStock(Product product, Long qty) {

        if ("DIRECT".equalsIgnoreCase(product.getTrackMode())) {

            product.setStockQty(product.getStockQty() + qty);
            productRepo.save(product);
            return;
        }

        List<ProductIngredient> recipe =
                productIngredientRepo.findByProduct_ProductId(product.getProductId());

        for (ProductIngredient r : recipe) {

            Ingredient ingredient = r.getIngredient();

            double restoreQty = r.getQtyRequired() * qty;

            ingredient.setStockQty(ingredient.getStockQty() + restoreQty);

            ingredientRepo.save(ingredient);
        }
    }
}