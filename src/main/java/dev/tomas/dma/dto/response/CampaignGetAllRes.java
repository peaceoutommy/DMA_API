package dev.tomas.dma.dto.response;

import dev.tomas.dma.dto.common.CampaignDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public class CampaignGetAllRes {
    public List<CampaignDTO> campaigns = new ArrayList<>();
}
