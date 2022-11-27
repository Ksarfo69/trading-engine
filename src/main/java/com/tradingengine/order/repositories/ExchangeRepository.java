package com.tradingengine.order.repositories;

import com.tradingengine.order.models.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
}
