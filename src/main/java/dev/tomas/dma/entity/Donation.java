package dev.tomas.dma.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Campaign campaign;

    @ManyToOne
    private User user;

    private Long amount;
    private LocalDateTime date = LocalDateTime.now();
}
