package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.common.DonationDTO;
import dev.tomas.dma.dto.response.DonationByUserGetAllRes;
import dev.tomas.dma.entity.Campaign;
import dev.tomas.dma.entity.Donation;
import dev.tomas.dma.entity.User;
import dev.tomas.dma.mapper.DonationMapper;
import dev.tomas.dma.repository.CampaignRepo;
import dev.tomas.dma.repository.DonationRepository;
import dev.tomas.dma.repository.UserRepo;
import dev.tomas.dma.service.DonationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {
    private final CampaignRepo campaignRepo;
    private final UserRepo userRepo;
    private final DonationMapper donationMapper;
    private final DonationRepository donationRepo;

    @Transactional
    public void save(DonationDTO dto) {
        Campaign campaign = campaignRepo.findById(dto.getCampaignId()).orElseThrow(() -> new EntityNotFoundException("Campaign not found with id: " + dto.getCampaignId()));
        User user = userRepo.findById(dto.getUserId()).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + dto.getUserId()));

        Donation donation = donationMapper.toEntity(dto);
        donationRepo.save(donation);

        campaign.setRaisedFunds(campaign.getRaisedFunds().add(new BigDecimal(dto.getAmount() / 100)));
        campaignRepo.save(campaign);
    }

    public List<DonationByUserGetAllRes> getAllByUserId(Integer userId){
        List<DonationByUserGetAllRes> dtoList = new ArrayList<>();

        List<Donation> donations = new ArrayList<>(donationRepo.findAllByUserId(userId));
        for(Donation entity : donations){
            DonationByUserGetAllRes dto = new DonationByUserGetAllRes();
            dto.setDate(entity.getDate());
            dto.setAmount(entity.getAmount());
            dto.setUserId(entity.getUser().getId());
            dto.setCampaignId(entity.getCampaign().getId());
            dto.setCompanyId(entity.getCampaign().getCompany().getId());
            dto.setCampaignName(entity.getCampaign().getName());
            dto.setCompanyName(entity.getCampaign().getCompany().getName());
            dtoList.add(dto);
        }
        return dtoList;
    }
}
