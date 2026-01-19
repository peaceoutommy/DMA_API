package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.request.CampaignCreateReq;
import dev.tomas.dma.dto.response.CampaignGetAllRes;
import dev.tomas.dma.dto.request.CampaignUpdateReq;
import dev.tomas.dma.dto.common.CampaignDTO;
import dev.tomas.dma.entity.AppFile;
import dev.tomas.dma.entity.Company;
import dev.tomas.dma.enums.CampaignStatus;
import dev.tomas.dma.enums.EntityType;
import dev.tomas.dma.enums.FileType;
import dev.tomas.dma.enums.Status;
import dev.tomas.dma.mapper.AppFileMapper;
import dev.tomas.dma.mapper.CampaignMapper;
import dev.tomas.dma.entity.Campaign;
import dev.tomas.dma.repository.AppFileRepo;
import dev.tomas.dma.repository.CampaignRepo;
import dev.tomas.dma.repository.CompanyRepo;
import dev.tomas.dma.repository.TicketRepo;
import dev.tomas.dma.service.CampaignService;
import dev.tomas.dma.service.ExternalStorageService;
import dev.tomas.dma.service.FileService;
import dev.tomas.dma.service.TicketService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.GrantedAuthority;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor

public class CampaignServiceImpl implements CampaignService {
    private final CampaignRepo campaignRepo;
    private final CompanyRepo companyRepo;
    private final CampaignMapper campaignMapper;
    private final TicketRepo ticketRepo;
    private final ExternalStorageService externalStorageService;
    private final FileService fileService;
    private final TicketService ticketService;
    private final AppFileRepo fileRepo;
    private final AppFileMapper fileMapper;

    @Override
    public CampaignGetAllRes findAll() {
        CampaignGetAllRes response = new CampaignGetAllRes();

        for (Campaign entity : campaignRepo.findAll()) {
            CampaignDTO dto = campaignMapper.entityToDTO(entity);
            List<AppFile> images = fileRepo.findByEntityTypeAndEntityIdAndFileType(EntityType.CAMPAIGN, dto.getId(), FileType.CAMPAIGN_IMAGE);

            dto.setFiles(fileMapper.entitiesToDTO(images));
            response.campaigns.add(dto);
        }
        return response;
    }

    public CampaignGetAllRes findAllApproved() {
        CampaignGetAllRes response = new CampaignGetAllRes();
        List<CampaignStatus> campaignStatuses = List.of(CampaignStatus.PENDING, CampaignStatus.APPROVED);
        List<Status> excludeTicketStatuses = List.of(Status.REJECTED, Status.PENDING);

        for (Campaign entity : campaignRepo.findAllByStatusIn(campaignStatuses)) {
            // Skip campaigns that have PENDING or REJECTED tickets
            if (ticketRepo.existsByEntityIdAndTypeAndStatusIn(
                    entity.getId(),
                    EntityType.CAMPAIGN,
                    excludeTicketStatuses)) {
                continue;  // Skip this campaign
            }

            CampaignDTO dto = campaignMapper.entityToDTO(entity);
            List<AppFile> images = fileRepo.findByEntityTypeAndEntityIdAndFileType(
                    EntityType.CAMPAIGN,
                    dto.getId(),
                    FileType.CAMPAIGN_IMAGE
            );

            dto.setFiles(fileMapper.entitiesToDTO(images));
            response.campaigns.add(dto);
        }

        return response;
    }

    @Override
    public CampaignGetAllRes findAllByStatus(CampaignStatus status) {
        CampaignGetAllRes response = new CampaignGetAllRes();

        for (Campaign entity : campaignRepo.findAllByStatus(status)) {
            CampaignDTO dto = campaignMapper.entityToDTO(entity);
            List<AppFile> images = fileRepo.findByEntityTypeAndEntityIdAndFileType(EntityType.CAMPAIGN, dto.getId(), FileType.CAMPAIGN_IMAGE);

            dto.setFiles(fileMapper.entitiesToDTO(images));
            response.campaigns.add(dto);
        }
        return response;
    }

    public CampaignGetAllRes findByCompanyId(Integer companyId) {
        Company company = companyRepo.findById(companyId).orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + companyId));
        CampaignGetAllRes dto = new CampaignGetAllRes();
        dto.setCampaigns(campaignMapper.entitiesToDTO(campaignRepo.findAllByCompanyId(companyId)));

        for(CampaignDTO campaignDTO : dto.campaigns){
            List<AppFile> images = fileRepo.findByEntityTypeAndEntityIdAndFileType(EntityType.CAMPAIGN, campaignDTO.getId(), FileType.CAMPAIGN_IMAGE);
            campaignDTO.setFiles(fileMapper.entitiesToDTO(images));
        }

        return dto;
    }

    @Override
    public CampaignDTO findById(Integer id) {
        Campaign entity = campaignRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Campaign not found with id: " + id));
        List<AppFile> images = fileRepo.findByEntityTypeAndEntityIdAndFileType(EntityType.CAMPAIGN, id, FileType.CAMPAIGN_IMAGE);

        CampaignDTO dto = campaignMapper.entityToDTO(entity);
        dto.setFiles(fileMapper.entitiesToDTO(images));
        return dto;
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

        ticketService.save(saved);

        if (request.getImages() != null) {
            String directoryName = saved.getCompany().getName() + "/" + saved.getName();
            externalStorageService.createFolder(directoryName);

            if (request.getImages().size() == 1) {
                String imgUrl;
                try {
                    imgUrl = externalStorageService.uploadFile(request.getImages().getFirst(), directoryName, "1");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                fileService.saveFile(saved.getId(), imgUrl, FileType.CAMPAIGN_IMAGE, EntityType.CAMPAIGN);
            }

            if (request.getImages().size() > 1) {
                int imageCount = 1;
                for (MultipartFile file : request.getImages()) {
                    String imgUrl;
                    try {
                        imgUrl = externalStorageService.uploadFile(file, directoryName, Integer.toString(imageCount));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    fileService.saveFile(saved.getId(), imgUrl, FileType.CAMPAIGN_IMAGE, EntityType.CAMPAIGN);
                    imageCount++;
                }
            }
        }
        return campaignMapper.entityToDTO(saved);
    }

    @Override
    public CampaignDTO update(CampaignUpdateReq request) {
        Campaign original = campaignRepo.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Campaign not found with id: " + request.getId()));

        if (request.getName() != null) {
            original.setName(request.getName());
        }
        if (request.getDescription() != null) {
            original.setDescription(request.getDescription());
        }
        if (request.getFundGoal() != null) {
            original.setFundGoal(request.getFundGoal());
        }
        if (request.getStartDate() != null) {
            original.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            original.setEndDate(request.getEndDate());
        }
        if (request.getStatus() != null) {
            original.setStatus(request.getStatus());
        }

        return campaignMapper.entityToDTO(campaignRepo.save(original));
    }

    public CampaignDTO archive(Integer id) {
        Campaign campaign = campaignRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Campaign not found with id: " + id));
        campaign.setStatus(CampaignStatus.ARCHIVED);
        return campaignMapper.entityToDTO(campaignRepo.save(campaign));
    }

    @Override
    public Integer deleteById(Integer id) {
        if (!campaignRepo.existsById(id)) {
            throw new EntityNotFoundException("Campaign not found with id: " + id);
        }
        campaignRepo.deleteById(id);
        return id;
    }
}
