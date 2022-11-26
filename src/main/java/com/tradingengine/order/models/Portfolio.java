package com.tradingengine.order.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Portfolio {
    @Id
    @SequenceGenerator(
            name = "portfolio_id_sequence",
            sequenceName = "portfolio_id_sequence"
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "portfolio_id_sequence"
    )
    private Long portfolioId;

    @Column(name = "portfolioName")
    private String portfolioName;

    @ManyToOne
    @JoinColumn(referencedColumnName = "username", nullable = false)
    private Client username;

    @Column(name = "balance")
    private Double balance;
}
