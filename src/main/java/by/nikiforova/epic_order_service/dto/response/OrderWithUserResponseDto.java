package by.nikiforova.epic_order_service.dto.response;

public record OrderWithUserResponseDto (OrderResponseDto order,
                                        UserInfoDto user) {
}
