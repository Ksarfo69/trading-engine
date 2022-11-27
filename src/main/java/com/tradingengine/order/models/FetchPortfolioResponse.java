package com.tradingengine.order.models;

public record FetchPortfolioResponse(
        String portfolioName,
        Double balance
) {
}
