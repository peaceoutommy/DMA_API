package dev.tomas.dma.service;

import dev.tomas.dma.dto.request.CampaignCreateReq;
import dev.tomas.dma.dto.response.CampaignGetAllRes;
import dev.tomas.dma.dto.request.CampaignUpdateReq;
import dev.tomas.dma.dto.common.CampaignDTO;

public interface CampaignService {
    CampaignGetAllRes findAll();
    CampaignDTO findById(Integer id);
    CampaignDTO save(CampaignCreateReq request);
    CampaignDTO update(CampaignUpdateReq request);
    Integer deleteById(Integer id);
}
