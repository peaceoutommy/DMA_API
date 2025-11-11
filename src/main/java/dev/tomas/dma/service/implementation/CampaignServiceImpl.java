package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.request.CampaignCreateReq;
import dev.tomas.dma.dto.response.CampaignGetAllRes;
import dev.tomas.dma.dto.request.CampaignUpdateReq;
import dev.tomas.dma.dto.common.CampaignDTO;
import dev.tomas.dma.enums.CampaignStatus;
import dev.tomas.dma.mapper.CampaignMapper;
import dev.tomas.dma.entity.Campaign;
import dev.tomas.dma.repository.CampaignRepo;
import dev.tomas.dma.repository.CompanyRepo;
import dev.tomas.dma.service.CampaignService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
@AllArgsConstructor

public class CampaignServiceImpl implements CampaignService {
    private final CampaignRepo campaignRepo;
    private final CompanyRepo companyRepo;
    private final CampaignMapper campaignMapper;

    @Override
    public CampaignGetAllRes findAll() {
        CampaignGetAllRes response = new CampaignGetAllRes();

        for (Campaign entity : campaignRepo.findAll()) {
            response.campaigns.add(campaignMapper.convertToDTO(entity));
        }
        return response;
    }

    @Override
    public CampaignDTO findById(Integer id) {
        Campaign entity = campaignRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Campaign not found with id: " + id));
        return campaignMapper.convertToDTO(entity);
    }

    @Override
    public CampaignDTO save(CampaignCreateReq request) {
        if (Objects.isNull(request.getName()) || request.getName().isEmpty()) {
            throw new IllegalArgumentException("Campaign name can't be empty");
        }
        if (Objects.isNull(request.getDescription()) || request.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Campaign description can't be empty");
        }

        Campaign toSave = new Campaign();
        toSave.setName(request.getName());
        toSave.setDescription(request.getDescription());
        toSave.setCompany(companyRepo.getReferenceById(request.getCompanyId()));
        toSave.setFundGoal(request.getFundGoal());
        toSave.setStatus(CampaignStatus.PENDING);
        if (request.getStartDate() != null) {
            toSave.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            toSave.setEndDate(request.getEndDate());
        }

        return campaignMapper.convertToDTO(campaignRepo.save(toSave));
    }

    @Override
    public CampaignDTO update(CampaignUpdateReq request) {
        Campaign original = campaignRepo.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Campaign not found with id: " + request.getId()));

        original.setName(request.getName());
        original.setDescription(request.getDescription());
        original.setFundGoal(request.getFundGoal());
        original.setStartDate(request.getStartDate());
        original.setEndDate(request.getEndDate());
        original.setStatus(request.getStatus());

        return campaignMapper.convertToDTO(campaignRepo.save(original));
    }

    public CampaignDTO archive(Integer id) {
        Campaign campaign = campaignRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Campaign not found with id: " + id));
        campaign.setStatus(CampaignStatus.ARCHIVED);
        return campaignMapper.convertToDTO(campaignRepo.save(campaign));
    }

    @Override
    public Integer deleteById(Integer id) {
        campaignRepo.deleteById(id);
        return id;
    }
}
