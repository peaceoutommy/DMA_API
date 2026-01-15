package dev.tomas.dma.service;

import dev.tomas.dma.dto.request.CampaignCreateReq;
import dev.tomas.dma.dto.response.CampaignGetAllRes;
import dev.tomas.dma.dto.request.CampaignUpdateReq;
import dev.tomas.dma.dto.common.CampaignDTO;
import dev.tomas.dma.enums.CampaignStatus;
import org.springframework.http.ResponseEntity;

public interface CampaignService {
    CampaignGetAllRes findAll();

    CampaignGetAllRes findAllApproved();

    CampaignGetAllRes findAllByStatus(CampaignStatus status);

    CampaignDTO findById(Integer id);

    CampaignDTO save(CampaignCreateReq request);

    CampaignDTO update(CampaignUpdateReq request);

    CampaignDTO archive(Integer id);

    Integer deleteById(Integer id);

    CampaignGetAllRes findByCompanyId(Integer companyId);
}
