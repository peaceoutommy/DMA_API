package dev.tomas.dma.dto.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonationDTO {
    private Integer campaignId;
    private Integer userId;
    private Long amount;
}
