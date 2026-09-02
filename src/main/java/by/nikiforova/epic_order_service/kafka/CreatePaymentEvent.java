package by.nikiforova.epic_order_service.kafka;

public record CreatePaymentEvent(String paymentId,
                                 Long orderId,
                                 String status) {
}
