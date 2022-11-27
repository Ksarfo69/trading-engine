package com.tradingengine.order.models;

import java.sql.Timestamp;

public record FetchExecutionResponse(
        Timestamp timestamp,
        Integer quantity,
        Double price
) {
}
