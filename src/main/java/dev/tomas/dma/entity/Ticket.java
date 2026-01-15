package dev.tomas.dma.entity;

import dev.tomas.dma.enums.EntityType;
import dev.tomas.dma.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer entityId;
    @Column(length = 5000)
    private String message;
    @Column(length = 10000)
    private String additionalInfo;
    private LocalDateTime closeDate;
    private LocalDateTime createDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private EntityType type;
    @Enumerated(EnumType.STRING)
    private Status status;
}
