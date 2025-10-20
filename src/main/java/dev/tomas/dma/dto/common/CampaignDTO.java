package dev.tomas.dma.dto.common;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CampaignDTO {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal fundGoal;
    private Integer companyId;
}
