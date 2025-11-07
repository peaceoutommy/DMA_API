package dev.tomas.dma.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Table(name = "campaign")
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Size(min = 3, max = 100, message = "Name must be at least 3 characters long")
    @Column(name = "name")
    private String name;

    @NotNull
    @Size(min = 10, max = 2000, message = "Description must be at least 10 characters long")
    @Column(name = "description")
    private String description;
    
    @ManyToOne()
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotNull
    @Min(1)
    @Max(999999999)
    private BigDecimal fundGoal;
}