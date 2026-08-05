package by.nikiforova.epic_order_service.specification;

import by.nikiforova.epic_order_service.entity.Order;
import by.nikiforova.epic_order_service.entity.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class OrderSpecification {

    public static Specification<Order> notDeleted() {
        return (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("deleted"), false);
    }

    public static Specification<Order> createdFrom(LocalDateTime createdFrom) {
        return (root, query, criteriaBuilder) -> {
            if (createdFrom == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdFrom);
        };
    }

    public static Specification<Order> createdTo(LocalDateTime createdTo) {
        return (root, query, criteriaBuilder) -> {
            if (createdTo == null) {
                return null;
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdTo);
        };
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    private OrderSpecification() {

    }
}
