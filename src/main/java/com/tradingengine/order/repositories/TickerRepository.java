package com.tradingengine.order.repositories;

import com.tradingengine.order.models.Ticker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TickerRepository extends JpaRepository<Ticker, String> {
}
