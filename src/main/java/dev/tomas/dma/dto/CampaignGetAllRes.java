package dev.tomas.dma.dto;

import dev.tomas.dma.model.Campaign;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public class CampaignGetAllRes {
    public List<Campaign> campaigns = new ArrayList<>();
}
