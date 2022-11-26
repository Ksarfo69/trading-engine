package com.tradingengine.order.repositories;

import com.tradingengine.order.models.Holding;
import com.tradingengine.order.models.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    List<Holding> findAllByPortfolio(Portfolio portfolio);

    Holding findOneByPortfolio(Portfolio portfolio);
}
