package com.tradingengine.order.repositories;

import com.tradingengine.order.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {
    Client findClientByUsername(String username);
}
