package com.tradingengine.order.models;

public record PortfolioRegistrationRequest (
        String portfolioName,
        Double balance
){
}
