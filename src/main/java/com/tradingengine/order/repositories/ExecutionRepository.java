package com.tradingengine.order.repositories;

import com.tradingengine.order.models.ClientOrder;
import com.tradingengine.order.models.Execution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExecutionRepository extends JpaRepository<Execution, Long> {
    List<Execution> findAllByClientOrder(ClientOrder clientOrder);
}
