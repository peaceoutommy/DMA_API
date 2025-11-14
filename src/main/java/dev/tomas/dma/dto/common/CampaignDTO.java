package dev.tomas.dma.dto.common;

import dev.tomas.dma.enums.CampaignStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class CampaignDTO {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal fundGoal;
    private BigDecimal raisedFunds;
    private LocalDate startDate;
    private LocalDate endDate;
    private CampaignStatus status;
    private Integer companyId;
    private List<String> images = new ArrayList<>();
}
