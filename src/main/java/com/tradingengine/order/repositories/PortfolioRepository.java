package com.tradingengine.order.repositories;

import com.tradingengine.order.models.Client;
import com.tradingengine.order.models.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findAllByClient(Client client);

    Portfolio findOneByClient(Client client);
}
