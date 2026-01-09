package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.common.DonationDTO;
import dev.tomas.dma.entity.Campaign;
import dev.tomas.dma.entity.Company;
import dev.tomas.dma.entity.Donation;
import dev.tomas.dma.entity.User;
import dev.tomas.dma.enums.CampaignStatus;
import dev.tomas.dma.mapper.DonationMapper;
import dev.tomas.dma.repository.CampaignRepo;
import dev.tomas.dma.repository.DonationRepository;
import dev.tomas.dma.repository.UserRepo;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonationServiceImplTest {

    @Mock
    private CampaignRepo campaignRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private DonationMapper donationMapper;

    @Mock
    private DonationRepository donationRepo;

    @InjectMocks
    private DonationServiceImpl donationService;

    private Campaign testCampaign;
    private User testUser;
    private DonationDTO donationDTO;
    private Donation testDonation;

    @BeforeEach
    void setUp() {
        Company testCompany = new Company();
        testCompany.setId(1);
        testCompany.setName("Test Company");

        testCampaign = new Campaign();
        testCampaign.setId(1);
        testCampaign.setName("Test Campaign");
        testCampaign.setCompany(testCompany);
        testCampaign.setFundGoal(new BigDecimal("10000"));
        testCampaign.setRaisedFunds(new BigDecimal("5000"));
        testCampaign.setStatus(CampaignStatus.ACTIVE);

        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("donor@example.com");

        donationDTO = new DonationDTO();
        donationDTO.setCampaignId(1);
        donationDTO.setUserId(1);
        donationDTO.setAmount(5000L); // Amount in cents (50.00)

        testDonation = new Donation();
        testDonation.setId(1);
        testDonation.setCampaign(testCampaign);
        testDonation.setUser(testUser);
        testDonation.setAmount(5000L);
    }

    @Nested
    @DisplayName("Save Donation Tests")
    class SaveDonationTests {

        @Test
        @DisplayName("Should save donation and update campaign raised funds")
        void save_Success() {
            when(campaignRepo.findById(1)).thenReturn(Optional.of(testCampaign));
            when(userRepo.findById(1)).thenReturn(Optional.of(testUser));
            when(donationMapper.toEntity(donationDTO)).thenReturn(testDonation);
            when(donationRepo.save(any(Donation.class))).thenReturn(testDonation);
            when(campaignRepo.save(any(Campaign.class))).thenReturn(testCampaign);

            donationService.save(donationDTO);

            verify(donationRepo).save(testDonation);
            verify(campaignRepo).save(argThat(campaign -> {
                // 5000 cents = 50.00, original raised = 5000, new total = 5050
                BigDecimal expectedRaised = new BigDecimal("5000").add(new BigDecimal("50"));
                return campaign.getRaisedFunds().compareTo(expectedRaised) == 0;
            }));
        }

        @Test
        @DisplayName("Should correctly convert cents to currency when updating raised funds")
        void save_ConvertsAmountCorrectly() {
            donationDTO.setAmount(10000L); // 100.00 in cents
            testCampaign.setRaisedFunds(new BigDecimal("0"));

            when(campaignRepo.findById(1)).thenReturn(Optional.of(testCampaign));
            when(userRepo.findById(1)).thenReturn(Optional.of(testUser));
            when(donationMapper.toEntity(donationDTO)).thenReturn(testDonation);
            when(donationRepo.save(any(Donation.class))).thenReturn(testDonation);
            when(campaignRepo.save(any(Campaign.class))).thenReturn(testCampaign);

            donationService.save(donationDTO);

            verify(campaignRepo).save(argThat(campaign ->
                    campaign.getRaisedFunds().compareTo(new BigDecimal("100")) == 0
            ));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when campaign not found")
        void save_ThrowsException_WhenCampaignNotFound() {
            when(campaignRepo.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> donationService.save(donationDTO))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Campaign not found with id: 1");

            verify(donationRepo, never()).save(any(Donation.class));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when user not found")
        void save_ThrowsException_WhenUserNotFound() {
            when(campaignRepo.findById(1)).thenReturn(Optional.of(testCampaign));
            when(userRepo.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> donationService.save(donationDTO))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("User not found with id: 1");

            verify(donationRepo, never()).save(any(Donation.class));
        }

        @Test
        @DisplayName("Should handle zero amount donation")
        void save_Success_WithZeroAmount() {
            donationDTO.setAmount(0L);
            testDonation.setAmount(0L);
            testCampaign.setRaisedFunds(new BigDecimal("5000"));

            when(campaignRepo.findById(1)).thenReturn(Optional.of(testCampaign));
            when(userRepo.findById(1)).thenReturn(Optional.of(testUser));
            when(donationMapper.toEntity(donationDTO)).thenReturn(testDonation);
            when(donationRepo.save(any(Donation.class))).thenReturn(testDonation);
            when(campaignRepo.save(any(Campaign.class))).thenReturn(testCampaign);

            donationService.save(donationDTO);

            verify(campaignRepo).save(argThat(campaign ->
                    campaign.getRaisedFunds().compareTo(new BigDecimal("5000")) == 0
            ));
        }

        @Test
        @DisplayName("Should handle large donation amounts")
        void save_Success_WithLargeAmount() {
            donationDTO.setAmount(999999900L); // 9,999,999.00 in cents
            testDonation.setAmount(999999900L);
            testCampaign.setRaisedFunds(new BigDecimal("0"));

            when(campaignRepo.findById(1)).thenReturn(Optional.of(testCampaign));
            when(userRepo.findById(1)).thenReturn(Optional.of(testUser));
            when(donationMapper.toEntity(donationDTO)).thenReturn(testDonation);
            when(donationRepo.save(any(Donation.class))).thenReturn(testDonation);
            when(campaignRepo.save(any(Campaign.class))).thenReturn(testCampaign);

            donationService.save(donationDTO);

            verify(donationRepo).save(testDonation);
            verify(campaignRepo).save(any(Campaign.class));
        }
    }
}
