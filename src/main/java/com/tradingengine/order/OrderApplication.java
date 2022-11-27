package com.tradingengine.order;

import com.tradingengine.order.models.Exchange;
import com.tradingengine.order.models.ExchangeStatus;
import com.tradingengine.order.repositories.ExchangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OrderApplication {
    @Autowired
    ExchangeRepository exchangeRepository;

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class);
    }

    @Bean
     CommandLineRunner runner() {
        return args -> {
            Exchange exchange = new Exchange(1l, "Exchange1", ExchangeStatus.ACTIVE);
            exchangeRepository.save(exchange);
        };
    }
}
