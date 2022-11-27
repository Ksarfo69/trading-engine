package com.tradingengine.order.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Holding {
    @Id
    @SequenceGenerator(
            name = "holding_id_sequence",
            sequenceName = "holding_id_sequence"
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "holding_id_sequence"
    )
    private Long holdingId;

    @ManyToOne
    @JoinColumn(referencedColumnName = "portfolioId", nullable = false)
    private Portfolio portfolio;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "accumulatedProfit")
    private Double accumulatedProfit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private HoldingStatus status;

    @Column(name = "timestamp")
    private LocalDate createdAt;

    @Column(name = "updatedAt")
    private LocalDate updatedAt;
}
