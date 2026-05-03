package com.example.backend.Service;

import com.example.backend.Model.KhqrPayment;
import com.example.backend.Model.Order;
import com.example.backend.Repository.KhqrPaymentRepository;
import com.example.backend.Repository.OrderRepository;
import com.example.backend.dto.KhqrCreateResponse;
import kh.gov.nbc.bakong_khqr.BakongKHQR;
import kh.gov.nbc.bakong_khqr.model.KHQRCurrency;
import kh.gov.nbc.bakong_khqr.model.KHQRData;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;
import kh.gov.nbc.bakong_khqr.model.MerchantInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class KhqrService {

    private final KhqrPaymentRepository khqrPaymentRepo;
    private final OrderRepository orderRepo;
    private final BakongService bakongService;
    private final TelegramService telegramService;

    @Value("${bakong.account-id}")
    private String bakongAccountId;

    @Value("${bakong.merchant-name}")
    private String merchantName;
    @Value("${bakong.merchant-id}")
    private String merchantId;
    @Value("${bakong.acquiring-bank}")
    private String acquiringBank;
    @Value("${bakong.merchant-city}")
    private String merchantCity;

    public KhqrService(
            KhqrPaymentRepository khqrPaymentRepo,
            OrderRepository orderRepo,
            BakongService bakongService,
            TelegramService telegramService
    ) {
        this.khqrPaymentRepo = khqrPaymentRepo;
        this.orderRepo = orderRepo;
        this.bakongService = bakongService;
        this.telegramService = telegramService;
    }

    @Transactional
    public KhqrCreateResponse createForOrder(Integer orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if ("PAID".equalsIgnoreCase(order.getOrderStatus())) {
            throw new RuntimeException("Order already paid");
        }

        KhqrPayment existing = khqrPaymentRepo.findByOrderId(orderId).orElse(null);
        if (existing != null && "PENDING".equalsIgnoreCase(existing.getStatus())) {
            return new KhqrCreateResponse(
                    existing.getQr(),
                    existing.getMd5(),
                    order.getTotalAmount(),
                    existing.getStatus()
            );
        }

        String billNumber = "ORD-" + order.getOrderId();

        MerchantInfo merchantInfo = new MerchantInfo();
        merchantInfo.setMerchantId(merchantId);
        merchantInfo.setAcquiringBank(acquiringBank);
        merchantInfo.setBakongAccountId(bakongAccountId);
        merchantInfo.setMerchantName(merchantName);
        merchantInfo.setMerchantCity(merchantCity);
        merchantInfo.setAmount(order.getTotalAmount());
        merchantInfo.setCurrency(KHQRCurrency.USD);
        merchantInfo.setBillNumber(billNumber);
        merchantInfo.setStoreLabel("POS");
        merchantInfo.setTerminalLabel("CASHIER-01");
        long expireAt = System.currentTimeMillis() + (5 * 60 * 1000);
        merchantInfo.setExpirationTimestamp(expireAt);
        KHQRResponse<KHQRData> response = BakongKHQR.generateMerchant(merchantInfo);

        if (response.getKHQRStatus().getCode() != 0) {
            throw new RuntimeException(
                    "KHQR generate failed: " + response.getKHQRStatus().getMessage()
            );
        }

        String qr = response.getData().getQr();
        String md5 = response.getData().getMd5();

        KhqrPayment payment = new KhqrPayment();
        payment.setOrderId(orderId);
        payment.setQr(qr);
        payment.setMd5(md5);
        payment.setStatus("PENDING");
        payment.setCreatedAt(LocalDateTime.now());

        khqrPaymentRepo.save(payment);

        return new KhqrCreateResponse(qr, md5, order.getTotalAmount(), "PENDING");
    }

    @Transactional
    public String checkStatus(Integer orderId) {
        KhqrPayment payment = khqrPaymentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("KHQR payment not found"));

        if ("PAID".equalsIgnoreCase(payment.getStatus())) {
            return "PAID";
        }

        Map<String, Object> result = bakongService.checkPayment(payment.getMd5());

        if (result == null) {
            return "PENDING";
        }

        String responseCode = String.valueOf(result.get("responseCode"));
        String status = String.valueOf(result.get("status"));

        boolean isPaid =
                "0".equals(responseCode)
                        || "COMPLETED".equalsIgnoreCase(status)
                        || "PAID".equalsIgnoreCase(status)
                        || "SUCCESS".equalsIgnoreCase(status);

        if (!isPaid) {
            return "PENDING";
        }

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"PAID".equalsIgnoreCase(order.getOrderStatus())) {
            order.setOrderStatus("PAID");
            orderRepo.save(order);
        }

        payment.setStatus("PAID");
        payment.setPaidAt(LocalDateTime.now());
        khqrPaymentRepo.save(payment);

        telegramService.sendMessage(
                "🧾 *PAYMENT RECEIVED*\n\n" +
                        "🏪 *Nexus POS*\n" +
                        "━━━━━━━━━━━━━━\n" +
                        "🆔 Order: #" + orderId + "\n" +
                        "💰 Amount: $" + order.getTotalAmount() + "\n" +
                        "💳 Method: KHQR\n" +
                        "👤 Customer: Walk-in\n" +
                        "⏰ Time: " + LocalDateTime.now() + "\n" +
                        "━━━━━━━━━━━━━━\n" +
                        "✅ Status: PAID"
        );

        return "PAID";
    }
}