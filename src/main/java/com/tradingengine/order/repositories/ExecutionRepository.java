package com.tradingengine.order.repositories;

import com.tradingengine.order.models.Execution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionRepository extends JpaRepository<Execution, Long> {
}
