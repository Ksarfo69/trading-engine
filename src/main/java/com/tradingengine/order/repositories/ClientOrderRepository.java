package com.tradingengine.order.repositories;

import com.tradingengine.order.models.ClientOrder;
import com.tradingengine.order.models.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientOrderRepository extends JpaRepository<ClientOrder, Long> {
    List<ClientOrder> findAllByHolding(Portfolio portfolio);
}
