package dev.tomas.dma.entity;

import dev.tomas.dma.enums.CampaignStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name="start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CampaignStatus status;
    
    @ManyToOne()
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotNull
    @Min(1)
    @Max(999999999)
    @Column(name = "fund_goal")
    private BigDecimal fundGoal;

    @Column(name = "raised_funds")
    private BigDecimal raisedFunds;

    @OneToMany(mappedBy = "campaign")
    private List<CampaignImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "campaign")
    private List<Donation> donations = new ArrayList<>();
}