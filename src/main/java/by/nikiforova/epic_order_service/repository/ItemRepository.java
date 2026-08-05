package by.nikiforova.epic_order_service.repository;

import by.nikiforova.epic_order_service.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

}
