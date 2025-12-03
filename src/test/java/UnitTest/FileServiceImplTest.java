package UnitTest;

import dev.tomas.dma.entity.AppFile;
import dev.tomas.dma.enums.EntityType;
import dev.tomas.dma.enums.FileType;
import dev.tomas.dma.repository.AppFileRepo;
import dev.tomas.dma.service.implementation.FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private AppFileRepo fileRepo;

    @InjectMocks
    private FileServiceImpl fileService;

    private AppFile testFile;

    @BeforeEach
    void setUp() {
        testFile = new AppFile();
        testFile.setId(1L);
        testFile.setUrl("http://example.com/image.jpg");
        testFile.setEntityId(1);
        testFile.setEntityType(EntityType.CAMPAIGN);
        testFile.setFileType(FileType.CAMPAIGN_IMAGE);
    }

    @Nested
    @DisplayName("SaveFile Tests")
    class SaveFileTests {

        @Test
        @DisplayName("Should save campaign image file")
        void saveFile_CampaignImage_Success() {
            when(fileRepo.save(any(AppFile.class))).thenAnswer(invocation -> {
                AppFile file = invocation.getArgument(0);
                file.setId(1L);
                return file;
            });

            AppFile result = fileService.saveFile(
                    1,
                    "http://cloudinary.com/campaign/image.jpg",
                    FileType.CAMPAIGN_IMAGE,
                    EntityType.CAMPAIGN
            );

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(fileRepo).save(argThat(file ->
                    file.getEntityId().equals(1) &&
                    file.getUrl().equals("http://cloudinary.com/campaign/image.jpg") &&
                    file.getFileType() == FileType.CAMPAIGN_IMAGE &&
                    file.getEntityType() == EntityType.CAMPAIGN
            ));
        }

        @Test
        @DisplayName("Should save profile picture file")
        void saveFile_ProfilePicture_Success() {
            when(fileRepo.save(any(AppFile.class))).thenAnswer(invocation -> {
                AppFile file = invocation.getArgument(0);
                file.setId(2L);
                return file;
            });

            AppFile result = fileService.saveFile(
                    5,
                    "http://cloudinary.com/users/profile.jpg",
                    FileType.PROFILE_PICTURE,
                    EntityType.USER
            );

            assertThat(result).isNotNull();
            verify(fileRepo).save(argThat(file ->
                    file.getEntityId().equals(5) &&
                    file.getFileType() == FileType.PROFILE_PICTURE &&
                    file.getEntityType() == EntityType.USER
            ));
        }

        @Test
        @DisplayName("Should save company picture file")
        void saveFile_CompanyPicture_Success() {
            when(fileRepo.save(any(AppFile.class))).thenAnswer(invocation -> {
                AppFile file = invocation.getArgument(0);
                file.setId(3L);
                return file;
            });

            AppFile result = fileService.saveFile(
                    10,
                    "http://cloudinary.com/companies/logo.png",
                    FileType.COMPANY_PICTURE,
                    EntityType.COMPANY
            );

            assertThat(result).isNotNull();
            verify(fileRepo).save(argThat(file ->
                    file.getEntityId().equals(10) &&
                    file.getFileType() == FileType.COMPANY_PICTURE &&
                    file.getEntityType() == EntityType.COMPANY
            ));
        }

        @Test
        @DisplayName("Should save campaign document file")
        void saveFile_CampaignDocument_Success() {
            when(fileRepo.save(any(AppFile.class))).thenAnswer(invocation -> {
                AppFile file = invocation.getArgument(0);
                file.setId(4L);
                return file;
            });

            AppFile result = fileService.saveFile(
                    3,
                    "http://cloudinary.com/campaign/report.pdf",
                    FileType.CAMPAIGN_DOCUMENT,
                    EntityType.CAMPAIGN
            );

            assertThat(result).isNotNull();
            verify(fileRepo).save(argThat(file ->
                    file.getFileType() == FileType.CAMPAIGN_DOCUMENT
            ));
        }

        @Test
        @DisplayName("Should handle various URL formats")
        void saveFile_VariousUrlFormats() {
            when(fileRepo.save(any(AppFile.class))).thenAnswer(invocation -> {
                AppFile file = invocation.getArgument(0);
                file.setId(1L);
                return file;
            });

            // Test with different URL formats
            String[] urls = {
                    "https://cloudinary.com/images/test.jpg",
                    "http://example.com/path/to/file.png",
                    "https://res.cloudinary.com/v1234567890/image/upload/test.gif"
            };

            for (String url : urls) {
                AppFile result = fileService.saveFile(1, url, FileType.CAMPAIGN_IMAGE, EntityType.CAMPAIGN);
                assertThat(result).isNotNull();
            }

            verify(fileRepo, times(3)).save(any(AppFile.class));
        }
    }
}
