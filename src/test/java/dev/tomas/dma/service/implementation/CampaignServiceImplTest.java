package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.common.CampaignDTO;
import dev.tomas.dma.dto.request.CampaignCreateReq;
import dev.tomas.dma.dto.request.CampaignUpdateReq;
import dev.tomas.dma.dto.response.CampaignGetAllRes;
import dev.tomas.dma.entity.AppFile;
import dev.tomas.dma.entity.Campaign;
import dev.tomas.dma.entity.Company;
import dev.tomas.dma.enums.CampaignStatus;
import dev.tomas.dma.enums.EntityType;
import dev.tomas.dma.enums.FileType;
import dev.tomas.dma.mapper.CampaignMapper;
import dev.tomas.dma.repository.AppFileRepo;
import dev.tomas.dma.repository.CampaignRepo;
import dev.tomas.dma.repository.CompanyRepo;
import dev.tomas.dma.service.ExternalStorageService;
import dev.tomas.dma.service.FileService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplTest {

    @Mock
    private CampaignRepo campaignRepo;

    @Mock
    private CompanyRepo companyRepo;

    @Mock
    private CampaignMapper campaignMapper;

    @Mock
    private ExternalStorageService externalStorageService;

    @Mock
    private FileService fileService;

    @Mock
    private AppFileRepo fileRepo;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    private Campaign testCampaign;
    private Company testCompany;
    private CampaignDTO testCampaignDTO;
    private CampaignCreateReq createRequest;
    private CampaignUpdateReq updateRequest;

    @BeforeEach
    void setUp() {
        testCompany = new Company();
        testCompany.setId(1);
        testCompany.setName("Test Company");

        testCampaign = new Campaign();
        testCampaign.setId(1);
        testCampaign.setName("Test Campaign");
        testCampaign.setDescription("Test Description for the campaign");
        testCampaign.setCompany(testCompany);
        testCampaign.setFundGoal(new BigDecimal("10000"));
        testCampaign.setRaisedFunds(new BigDecimal("5000"));
        testCampaign.setStatus(CampaignStatus.ACTIVE);
        testCampaign.setStartDate(LocalDate.now());
        testCampaign.setEndDate(LocalDate.now().plusMonths(3));

        testCampaignDTO = new CampaignDTO();
        testCampaignDTO.setId(1);
        testCampaignDTO.setName("Test Campaign");
        testCampaignDTO.setDescription("Test Description for the campaign");
        testCampaignDTO.setCompanyId(1);
        testCampaignDTO.setFundGoal(new BigDecimal("10000"));
        testCampaignDTO.setRaisedFunds(new BigDecimal("5000"));
        testCampaignDTO.setStatus(CampaignStatus.ACTIVE);

        createRequest = new CampaignCreateReq();
        createRequest.setName("New Campaign");
        createRequest.setDescription("New campaign description here");
        createRequest.setCompanyId(1);
        createRequest.setFundGoal(new BigDecimal("20000"));
        createRequest.setStartDate(LocalDate.now());
        createRequest.setEndDate(LocalDate.now().plusMonths(6));

        updateRequest = new CampaignUpdateReq();
        updateRequest.setId(1);
        updateRequest.setName("Updated Campaign");
        updateRequest.setDescription("Updated description here");
        updateRequest.setCompanyId(1);
        updateRequest.setFundGoal(new BigDecimal("15000"));
        updateRequest.setStatus(CampaignStatus.ACTIVE);
    }

    @Nested
    @DisplayName("FindAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should return all campaigns")
        void findAll_Success() {
            List<Campaign> campaigns = Arrays.asList(testCampaign);
            when(campaignRepo.findAll()).thenReturn(campaigns);
            when(campaignMapper.convertToDTO(any(Campaign.class))).thenReturn(testCampaignDTO);
            when(fileRepo.findByEntityTypeAndEntityIdAndFileType(
                    eq(EntityType.CAMPAIGN), anyInt(), eq(FileType.CAMPAIGN_IMAGE)))
                    .thenReturn(new ArrayList<>());

            CampaignGetAllRes result = campaignService.findAll();

            assertThat(result).isNotNull();
            assertThat(result.getCampaigns()).hasSize(1);
            verify(campaignRepo).findAll();
        }

        @Test
        @DisplayName("Should return campaigns with images")
        void findAll_WithImages() {
            AppFile imageFile = new AppFile();
            imageFile.setUrl("http://example.com/image.jpg");

            when(campaignRepo.findAll()).thenReturn(Arrays.asList(testCampaign));
            when(campaignMapper.convertToDTO(any(Campaign.class))).thenReturn(testCampaignDTO);
            when(fileRepo.findByEntityTypeAndEntityIdAndFileType(
                    eq(EntityType.CAMPAIGN), anyInt(), eq(FileType.CAMPAIGN_IMAGE)))
                    .thenReturn(Arrays.asList(imageFile));

            CampaignGetAllRes result = campaignService.findAll();

            assertThat(result.getCampaigns().get(0).getImages()).contains("http://example.com/image.jpg");
        }

        @Test
        @DisplayName("Should return empty list when no campaigns exist")
        void findAll_EmptyList() {
            when(campaignRepo.findAll()).thenReturn(new ArrayList<>());

            CampaignGetAllRes result = campaignService.findAll();

            assertThat(result.getCampaigns()).isEmpty();
        }
    }

    @Nested
    @DisplayName("FindById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return campaign by id")
        void findById_Success() {
            when(campaignRepo.findById(1)).thenReturn(Optional.of(testCampaign));
            when(campaignMapper.convertToDTO(testCampaign)).thenReturn(testCampaignDTO);
            when(fileRepo.findByEntityTypeAndEntityIdAndFileType(
                    eq(EntityType.CAMPAIGN), eq(1), eq(FileType.CAMPAIGN_IMAGE)))
                    .thenReturn(new ArrayList<>());

            CampaignDTO result = campaignService.findById(1);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("Test Campaign");
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when campaign not found")
        void findById_ThrowsException_WhenNotFound() {
            when(campaignRepo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> campaignService.findById(999))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Campaign not found with id: 999");
        }
    }

    @Nested
    @DisplayName("Save Tests")
    class SaveTests {

        @Test
        @DisplayName("Should save campaign successfully without images")
        void save_Success_WithoutImages() {
            createRequest.setImages(null);

            when(companyRepo.getReferenceById(1)).thenReturn(testCompany);
            when(campaignRepo.save(any(Campaign.class))).thenAnswer(invocation -> {
                Campaign campaign = invocation.getArgument(0);
                campaign.setId(1);
                campaign.setCompany(testCompany);
                return campaign;
            });
            when(campaignMapper.convertToDTO(any(Campaign.class))).thenReturn(testCampaignDTO);

            CampaignDTO result = campaignService.save(createRequest);

            assertThat(result).isNotNull();
            verify(campaignRepo).save(any(Campaign.class));
            verify(externalStorageService, never()).createFolder(anyString());
        }

        @Test
        @DisplayName("Should save campaign with single image")
        void save_Success_WithSingleImage() throws IOException {
            MockMultipartFile image = new MockMultipartFile(
                    "image", "test.jpg", "image/jpeg", "test image content".getBytes());
            createRequest.setImages(Arrays.asList(image));

            when(companyRepo.getReferenceById(1)).thenReturn(testCompany);
            when(campaignRepo.save(any(Campaign.class))).thenAnswer(invocation -> {
                Campaign campaign = invocation.getArgument(0);
                campaign.setId(1);
                campaign.setCompany(testCompany);
                return campaign;
            });
            when(externalStorageService.uploadFile(any(MultipartFile.class), anyString(), anyString()))
                    .thenReturn("http://example.com/image.jpg");
            when(campaignMapper.convertToDTO(any(Campaign.class))).thenReturn(testCampaignDTO);

            CampaignDTO result = campaignService.save(createRequest);

            assertThat(result).isNotNull();
            verify(externalStorageService).createFolder(contains("Test Company"));
            verify(externalStorageService).uploadFile(any(MultipartFile.class), anyString(), eq("1"));
            verify(fileService).saveFile(eq(1), anyString(), eq(FileType.CAMPAIGN_IMAGE), eq(EntityType.CAMPAIGN));
        }

        @Test
        @DisplayName("Should save campaign with multiple images")
        void save_Success_WithMultipleImages() throws IOException {
            MockMultipartFile image1 = new MockMultipartFile(
                    "image1", "test1.jpg", "image/jpeg", "test image 1".getBytes());
            MockMultipartFile image2 = new MockMultipartFile(
                    "image2", "test2.jpg", "image/jpeg", "test image 2".getBytes());
            createRequest.setImages(Arrays.asList(image1, image2));

            when(companyRepo.getReferenceById(1)).thenReturn(testCompany);
            when(campaignRepo.save(any(Campaign.class))).thenAnswer(invocation -> {
                Campaign campaign = invocation.getArgument(0);
                campaign.setId(1);
                campaign.setCompany(testCompany);
                return campaign;
            });
            when(externalStorageService.uploadFile(any(MultipartFile.class), anyString(), anyString()))
                    .thenReturn("http://example.com/image.jpg");
            when(campaignMapper.convertToDTO(any(Campaign.class))).thenReturn(testCampaignDTO);

            CampaignDTO result = campaignService.save(createRequest);

            assertThat(result).isNotNull();
            verify(externalStorageService, times(2)).uploadFile(any(MultipartFile.class), anyString(), anyString());
            verify(fileService, times(2)).saveFile(anyInt(), anyString(), eq(FileType.CAMPAIGN_IMAGE), eq(EntityType.CAMPAIGN));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when name is empty")
        void save_ThrowsException_WhenNameEmpty() {
            createRequest.setName("");

            assertThatThrownBy(() -> campaignService.save(createRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Campaign name can't be empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when name is null")
        void save_ThrowsException_WhenNameNull() {
            createRequest.setName(null);

            assertThatThrownBy(() -> campaignService.save(createRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Campaign name can't be empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when description is empty")
        void save_ThrowsException_WhenDescriptionEmpty() {
            createRequest.setDescription("");

            assertThatThrownBy(() -> campaignService.save(createRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Campaign description can't be empty");
        }
    }

    @Nested
    @DisplayName("Update Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update campaign successfully")
        void update_Success() {
            when(campaignRepo.findById(1)).thenReturn(Optional.of(testCampaign));
            when(campaignRepo.save(any(Campaign.class))).thenReturn(testCampaign);
            when(campaignMapper.convertToDTO(any(Campaign.class))).thenReturn(testCampaignDTO);

            CampaignDTO result = campaignService.update(updateRequest);

            assertThat(result).isNotNull();
            verify(campaignRepo).save(argThat(campaign -> 
                campaign.getName().equals("Updated Campaign") &&
                campaign.getDescription().equals("Updated description here")
            ));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when campaign not found")
        void update_ThrowsException_WhenNotFound() {
            updateRequest.setId(999);
            when(campaignRepo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> campaignService.update(updateRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Campaign not found with id: 999");
        }
    }

    @Nested
    @DisplayName("Archive Tests")
    class ArchiveTests {

        @Test
        @DisplayName("Should archive campaign successfully")
        void archive_Success() {
            when(campaignRepo.findById(1)).thenReturn(Optional.of(testCampaign));
            when(campaignRepo.save(any(Campaign.class))).thenReturn(testCampaign);
            when(campaignMapper.convertToDTO(any(Campaign.class))).thenReturn(testCampaignDTO);

            CampaignDTO result = campaignService.archive(1);

            assertThat(result).isNotNull();
            verify(campaignRepo).save(argThat(campaign -> 
                campaign.getStatus() == CampaignStatus.ARCHIVED
            ));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when campaign not found")
        void archive_ThrowsException_WhenNotFound() {
            when(campaignRepo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> campaignService.archive(999))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Campaign not found with id: 999");
        }
    }

    @Nested
    @DisplayName("DeleteById Tests")
    class DeleteByIdTests {

        @Test
        @DisplayName("Should delete campaign successfully")
        void deleteById_Success() {
            doNothing().when(campaignRepo).deleteById(1);

            Integer result = campaignService.deleteById(1);

            assertThat(result).isEqualTo(1);
            verify(campaignRepo).deleteById(1);
        }
    }
}
