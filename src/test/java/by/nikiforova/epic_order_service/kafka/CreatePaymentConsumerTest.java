package by.nikiforova.epic_order_service.kafka;

import by.nikiforova.epic_order_service.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreatePaymentConsumerTest {
    @Mock
    private OrderService orderService;

    @InjectMocks
    private CreatePaymentConsumer createPaymentConsumer;

    @Test
    @DisplayName("handleCreatePayment - delegates to OrderService")
    void handleCreatePaymentShouldCallUpdateStatusFromPayment() {

        CreatePaymentEvent event = new CreatePaymentEvent("newId1", 10L, "SUCCESS");
        createPaymentConsumer.handleCreatePayment(event);

        verify(orderService).updateStatusFromPayment(10L, "SUCCESS");
    }
}
