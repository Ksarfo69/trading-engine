package com.tradingengine.order.repositories;

import com.tradingengine.order.models.Ticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TickerRepository extends JpaRepository<Ticker, String> {
     Ticker findTickerByTickerName(String tickerName);
}
