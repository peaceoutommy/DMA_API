package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.request.CampaignCreateReq;
import dev.tomas.dma.dto.response.CampaignGetAllRes;
import dev.tomas.dma.dto.request.CampaignUpdateReq;
import dev.tomas.dma.dto.common.CampaignDTO;
import dev.tomas.dma.entity.AppFile;
import dev.tomas.dma.enums.CampaignStatus;
import dev.tomas.dma.enums.EntityType;
import dev.tomas.dma.enums.FileType;
import dev.tomas.dma.mapper.CampaignMapper;
import dev.tomas.dma.entity.Campaign;
import dev.tomas.dma.repository.AppFileRepo;
import dev.tomas.dma.repository.CampaignRepo;
import dev.tomas.dma.repository.CompanyRepo;
import dev.tomas.dma.service.CampaignService;
import dev.tomas.dma.service.ExternalStorageService;
import dev.tomas.dma.service.FileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;

@Service
@AllArgsConstructor

public class CampaignServiceImpl implements CampaignService {
    private final CampaignRepo campaignRepo;
    private final CompanyRepo companyRepo;
    private final CampaignMapper campaignMapper;
    private final ExternalStorageService externalStorageService;
    private final FileService fileService;
    private final AppFileRepo fileRepo;

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
    @Transactional
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
        toSave.setRaisedFunds(BigDecimal.valueOf(0));

        if (request.getStartDate() != null) {
            toSave.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            toSave.setEndDate(request.getEndDate());
        }

        Campaign saved = campaignRepo.save(toSave);

        if (request.getImages() != null) {
            String directoryName = saved.getCompany().getName() + "/" + saved.getName();
            externalStorageService.createFolder(directoryName);

            if (request.getImages().size() == 1) {
                String imgUrl = null;
                try {
                    imgUrl = externalStorageService.uploadFile(request.getImages().get(1), directoryName, "1");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                fileService.saveFile(saved.getId(), imgUrl, FileType.CAMPAIGN_IMAGE, EntityType.CAMPAIGN);
            }

            if (request.getImages().size() > 1) {
                Integer imageCount = 1;
                for (MultipartFile file : request.getImages()) {
                    String imgUrl = null;
                    try {
                        imgUrl = externalStorageService.uploadFile(file, directoryName, imageCount.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    fileService.saveFile(saved.getId(), imgUrl, FileType.CAMPAIGN_IMAGE, EntityType.CAMPAIGN);
                    imageCount++;
                }
            }
        }

        return campaignMapper.convertToDTO(saved);
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
