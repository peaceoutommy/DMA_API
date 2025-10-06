package dev.tomas.dma.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Currency;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data // Getters and setters in 1
@Table(name = "campaign")
public class CampaignEntity {
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

    @NotNull
    @Column(name = "company_id")
    private Integer companyId;

    @NotNull
    @Min(1)
    @Max(999999999)
    private BigDecimal fundGoal;
}