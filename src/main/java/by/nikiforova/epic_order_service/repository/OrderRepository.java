package by.nikiforova.epic_order_service.repository;

import by.nikiforova.epic_order_service.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByIdAndDeletedFalse(Long id);
    List<Order> findByUserIdAndDeletedFalse(Long userId);
    Page<Order> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);
}
