package dev.tomas.dma.service.implementation;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.api.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceImplTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Api api;

    @Mock
    private Uploader uploader;

    @Mock
    private ApiResponse apiResponse;

    @InjectMocks
    private CloudinaryServiceImpl cloudinaryService;

    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() {
        testFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
    }

    @Nested
    @DisplayName("CreateFolder Tests")
    class CreateFolderTests {

        @Test
        @DisplayName("Should create folder successfully")
        void createFolder_Success() throws Exception {
            when(cloudinary.api()).thenReturn(api);
            when(api.createFolder(anyString(), isNull())).thenReturn(apiResponse);

            cloudinaryService.createFolder("test-folder");

            verify(api).createFolder("test-folder", null);
        }

        @Test
        @DisplayName("Should handle folder already exists error silently")
        void createFolder_FolderAlreadyExists() throws Exception {
            when(cloudinary.api()).thenReturn(api);
            Exception existsException = new Exception("Folder already exists");
            when(api.createFolder(anyString(), isNull())).thenThrow(existsException);

            // Should not throw exception when folder already exists
            cloudinaryService.createFolder("existing-folder");

            verify(api).createFolder("existing-folder", null);
        }

        @Test
        @DisplayName("Should throw RuntimeException for other errors")
        void createFolder_ThrowsException_ForOtherErrors() throws Exception {
            when(cloudinary.api()).thenReturn(api);
            Exception otherException = new Exception("Some other error");
            when(api.createFolder(anyString(), isNull())).thenThrow(otherException);

            assertThatThrownBy(() -> cloudinaryService.createFolder("test-folder"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to create Cloudinary folder: test-folder");
        }

        @Test
        @DisplayName("Should handle nested folder paths")
        void createFolder_NestedPath() throws Exception {
            when(cloudinary.api()).thenReturn(api);
            when(api.createFolder(anyString(), isNull())).thenReturn(apiResponse);

            cloudinaryService.createFolder("company/campaigns/images");

            verify(api).createFolder("company/campaigns/images", null);
        }
    }

    @Nested
    @DisplayName("UploadFile Tests")
    class UploadFileTests {

        @Test
        @DisplayName("Should upload file successfully and return secure URL")
        void uploadFile_Success() throws Exception {
            Map<String, Object> uploadResult = new HashMap<>();
            uploadResult.put("secure_url", "https://cloudinary.com/test/image.jpg");

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

            String result = cloudinaryService.uploadFile(testFile, "test-directory", "test-file");

            assertThat(result).isEqualTo("https://cloudinary.com/test/image.jpg");
        }

        @Test
        @DisplayName("Should set correct upload parameters")
        void uploadFile_CorrectParams() throws Exception {
            Map<String, Object> uploadResult = new HashMap<>();
            uploadResult.put("secure_url", "https://cloudinary.com/test/image.jpg");

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), argThat(params ->
                    params.get("folder").equals("campaigns") &&
                            params.get("public_id").equals("image1") &&
                            params.get("resource_type").equals("auto")
            ))).thenReturn(uploadResult);

            String result = cloudinaryService.uploadFile(testFile, "campaigns", "image1");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw IOException when upload fails")
        void uploadFile_ThrowsException_WhenUploadFails() throws Exception {
            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), anyMap()))
                    .thenThrow(new IOException("Upload failed"));

            assertThatThrownBy(() -> cloudinaryService.uploadFile(testFile, "test-dir", "test-file"))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("Failed to upload file test-file to directory: test-dir");
        }

        @Test
        @DisplayName("Should handle different file types")
        void uploadFile_DifferentFileTypes() throws Exception {
            Map<String, Object> uploadResult = new HashMap<>();
            uploadResult.put("secure_url", "https://cloudinary.com/test/file.pdf");

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

            MockMultipartFile pdfFile = new MockMultipartFile(
                    "document",
                    "document.pdf",
                    "application/pdf",
                    "pdf content".getBytes()
            );

            String result = cloudinaryService.uploadFile(pdfFile, "documents", "doc1");

            assertThat(result).isEqualTo("https://cloudinary.com/test/file.pdf");
        }

        @Test
        @DisplayName("Should handle empty file name")
        void uploadFile_EmptyFileName() throws Exception {
            Map<String, Object> uploadResult = new HashMap<>();
            uploadResult.put("secure_url", "https://cloudinary.com/test/file.jpg");

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

            String result = cloudinaryService.uploadFile(testFile, "test-dir", "");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle special characters in directory name")
        void uploadFile_SpecialCharactersInDirectory() throws Exception {
            Map<String, Object> uploadResult = new HashMap<>();
            uploadResult.put("secure_url", "https://cloudinary.com/test/file.jpg");

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

            String result = cloudinaryService.uploadFile(testFile, "Company Name/Campaign 2024", "image");

            assertThat(result).isNotNull();
            verify(uploader).upload(any(byte[].class), argThat(params ->
                    params.get("folder").equals("Company Name/Campaign 2024")
            ));
        }

        @Test
        @DisplayName("Should handle large files")
        void uploadFile_LargeFile() throws Exception {
            byte[] largeContent = new byte[10 * 1024 * 1024]; // 10MB
            MockMultipartFile largeFile = new MockMultipartFile(
                    "large-file",
                    "large-image.jpg",
                    "image/jpeg",
                    largeContent
            );

            Map<String, Object> uploadResult = new HashMap<>();
            uploadResult.put("secure_url", "https://cloudinary.com/test/large.jpg");

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

            String result = cloudinaryService.uploadFile(largeFile, "uploads", "large");

            assertThat(result).isNotNull();
        }
    }
}