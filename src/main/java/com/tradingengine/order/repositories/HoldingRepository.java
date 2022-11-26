package com.tradingengine.order.repositories;

import com.tradingengine.order.models.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
}
