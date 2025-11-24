package dev.tomas.dma.entity;

import dev.tomas.dma.enums.TicketType;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private TicketType type;
    @OneToOne
    private Company company;
    @OneToOne
    private Campaign campaign;
    private BigDecimal amount;
}