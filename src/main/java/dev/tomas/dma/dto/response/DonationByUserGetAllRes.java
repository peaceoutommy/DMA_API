package dev.tomas.dma.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DonationByUserGetAllRes {
    private Integer campaignId;
    private Integer userId;
    private Integer companyId;
    private String campaignName;
    private String companyName;
    private Long amount;
    private LocalDateTime date;
}
