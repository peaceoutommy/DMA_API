package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.CampaignCreateRequest;
import dev.tomas.dma.dto.CampaignGetAllResponseDto;
import dev.tomas.dma.dto.CampaignUpdateRequest;
import dev.tomas.dma.mapper.CampaignMapper;
import dev.tomas.dma.model.Campaign;
import dev.tomas.dma.model.entity.CampaignEntity;
import dev.tomas.dma.repository.CampaignRepo;
import dev.tomas.dma.service.CampaignService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor

public class CampaignServiceImpl implements CampaignService {
    CampaignRepo campaignRepo;

    @Override
    public CampaignGetAllResponseDto findAll() {
        CampaignGetAllResponseDto response = new CampaignGetAllResponseDto();
        for (CampaignEntity entity : campaignRepo.findAll()) {
            response.campaigns.add(CampaignMapper.INSTANCE.convertToModel(entity));
        }
        return response;
    }

    @Override
    public Campaign findById(Integer id) {
        if (campaignRepo.findById(id).isPresent()) {
            return CampaignMapper.INSTANCE.convertToModel(campaignRepo.findById(id).get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found");
        }
    }

    @Override
    public Campaign save(CampaignCreateRequest request) {
        if (Objects.isNull(request.getName()) || request.getName().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign name can't be empty");
        }
        if (Objects.isNull(request.getDescription()) || request.getDescription().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign description can't be empty");
        }

        CampaignEntity toSave = new CampaignEntity();
        toSave.setName(request.getName());
        toSave.setDescription(request.getDescription());
        toSave.setCompanyId(request.getCompanyId());
        toSave.setFundGoal(request.getFundGoal());

        return CampaignMapper.INSTANCE.convertToModel(campaignRepo.save(toSave));
    }

    @Override
    public Campaign update(CampaignUpdateRequest request) {
        CampaignEntity original = campaignRepo.findById(request.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));

        if (request.getName() != null && !request.getName().isEmpty()) {
            original.setName(request.getName());
        }
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            original.setDescription(request.getDescription());
        }
        if (request.getCompanyId() != null) {
            original.setCompanyId(request.getCompanyId());
        }
        if (request.getFundGoal() != null) {
            original.setFundGoal(request.getFundGoal());
        }

        CampaignEntity updated = campaignRepo.save(original);
        return CampaignMapper.INSTANCE.convertToModel(updated);
    }

    @Override
    public Integer deleteById(Integer id) {
        campaignRepo.deleteById(id);
        return id;
    }
}
