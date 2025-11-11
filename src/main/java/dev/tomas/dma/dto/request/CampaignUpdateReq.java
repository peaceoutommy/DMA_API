package dev.tomas.dma.dto.request;

import dev.tomas.dma.enums.CampaignStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CampaignUpdateReq {
    @NotNull
    Integer id;
    @Size(min = 3, max = 100, message = "Name must be at least 3 characters long")
    private String name;

    @NotBlank
    @Size(min = 10, max = 2000, message = "Description must be at least 10 characters long")
    private String description;

    @NotNull
    private Integer companyId;

    @NotNull
    @Min(1) @Max(999999999)
    private BigDecimal fundGoal;

    private LocalDate startDate;
    private LocalDate endDate;
    private CampaignStatus status;

}
