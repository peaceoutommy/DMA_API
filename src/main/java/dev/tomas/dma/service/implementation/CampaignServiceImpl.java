package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.request.CampaignCreateReq;
import dev.tomas.dma.dto.response.CampaignGetAllRes;
import dev.tomas.dma.dto.request.CampaignUpdateReq;
import dev.tomas.dma.dto.common.CampaignDTO;
import dev.tomas.dma.mapper.CampaignMapper;
import dev.tomas.dma.entity.Campaign;
import dev.tomas.dma.repository.CampaignRepo;
import dev.tomas.dma.repository.CompanyRepo;
import dev.tomas.dma.service.CampaignService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
@AllArgsConstructor

public class CampaignServiceImpl implements CampaignService {
    private final CampaignRepo campaignRepo;
    private final CompanyRepo companyRepo;

    @Override
    public CampaignGetAllRes findAll() {
        CampaignGetAllRes response = new CampaignGetAllRes();

        for (Campaign entity : campaignRepo.findAll()) {
            response.campaigns.add(CampaignMapper.INSTANCE.convertToDTO(entity));
        }
        return response;
    }

    @Override
    public CampaignDTO findById(Integer id) {
        if (campaignRepo.findById(id).isPresent()) {
            return CampaignMapper.INSTANCE.convertToDTO(campaignRepo.findById(id).get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found");
        }
    }

    @Override
    public CampaignDTO save(CampaignCreateReq request) {
        if (Objects.isNull(request.getName()) || request.getName().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign name can't be empty");
        }
        if (Objects.isNull(request.getDescription()) || request.getDescription().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign description can't be empty");
        }

        Campaign toSave = new Campaign();
        toSave.setName(request.getName());
        toSave.setDescription(request.getDescription());
        toSave.setCompany(companyRepo.getReferenceById(request.getCompanyId()));
        toSave.setFundGoal(request.getFundGoal());

        return CampaignMapper.INSTANCE.convertToDTO(campaignRepo.save(toSave));
    }

    @Override
    public CampaignDTO update(CampaignUpdateReq request) {
        Campaign original = campaignRepo.findById(request.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));

        if (request.getName() != null && !request.getName().isEmpty()) {
            original.setName(request.getName());
        }
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            original.setDescription(request.getDescription());
        }
        if (request.getCompanyId() != null) {
            original.setCompany(companyRepo.getReferenceById(request.getCompanyId()));
        }
        if (request.getFundGoal() != null) {
            original.setFundGoal(request.getFundGoal());
        }

        Campaign updated = campaignRepo.save(original);
        return CampaignMapper.INSTANCE.convertToDTO(updated);
    }

    @Override
    public Integer deleteById(Integer id) {
        campaignRepo.deleteById(id);
        return id;
    }
}
