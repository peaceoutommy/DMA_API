package dev.tomas.dma.service;

import dev.tomas.dma.dto.request.CampaignCreateReq;
import dev.tomas.dma.dto.response.CampaignGetAllRes;
import dev.tomas.dma.dto.request.CampaignUpdateReq;
import dev.tomas.dma.dto.common.CampaignDTO;
import org.springframework.http.ResponseEntity;

public interface CampaignService {
    ResponseEntity<CampaignGetAllRes> findAll();

    ResponseEntity<CampaignDTO> findById(Integer id);

    ResponseEntity<CampaignDTO> save(CampaignCreateReq request);

    ResponseEntity<CampaignDTO> update(CampaignUpdateReq request);

    Integer deleteById(Integer id);
}
