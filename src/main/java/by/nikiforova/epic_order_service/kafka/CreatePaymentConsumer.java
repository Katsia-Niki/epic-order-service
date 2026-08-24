package by.nikiforova.epic_order_service.kafka;

import by.nikiforova.epic_order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreatePaymentConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "${kafka.topic.create-payment}", groupId = "order-service")
    public void handleCreatePayment(CreatePaymentEvent event) {
        log.info("Received CreatePayment Event orderId={}, status={}", event.orderId(), event.status());

        orderService.updateStatusFromPayment(event.orderId(), event.status());
    }
}
