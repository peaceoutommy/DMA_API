package dev.tomas.dma.dto.common;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DonationDTO {
    private Integer campaignId;
    private Integer userId;
    private Long amount;
    private LocalDateTime date;
}
