package by.nikiforova.epic_order_service.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    public static final String TIMEZONE = "Europe/Minsk";
    public static final String ORDER_NOT_FOUND = "Order not found ";
    public static final String ITEM_NOT_FOUND = "Item not found ";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String ORDERS_CACHE = "orders";
    public static final String PLACEHOLDER_NAME = "Unavailable";
    public static final String PLACEHOLDER_EMAIL = "unavailable@mail";
    public static final String USERS_BASE_PATH = "/api/users";

}
