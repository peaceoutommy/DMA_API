package dev.tomas.dma.service;

import dev.tomas.dma.dto.CampaignCreateReq;
import dev.tomas.dma.dto.CampaignGetAllRes;
import dev.tomas.dma.dto.CampaignUpdateReq;
import dev.tomas.dma.model.Campaign;

public interface CampaignService {
    CampaignGetAllRes findAll();
    Campaign findById(Integer id);
    Campaign save(CampaignCreateReq request);
    Campaign update(CampaignUpdateReq request);
    Integer deleteById(Integer id);
}
