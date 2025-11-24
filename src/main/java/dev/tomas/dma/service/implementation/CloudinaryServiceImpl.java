package dev.tomas.dma.service.implementation;

import com.cloudinary.Cloudinary;
import dev.tomas.dma.service.ExternalStorageService;
import dev.tomas.dma.service.FileService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class CloudinaryServiceImpl implements ExternalStorageService {
    private final Cloudinary cloudinary;

    public void createFolder(String folderName) {
        try {
            cloudinary.api().createFolder(folderName, null);
        } catch (Exception e) {
            if (e.getMessage().contains("already exists")) {
                return;
            }
            throw new RuntimeException("Failed to create Cloudinary folder: " + folderName, e);
        }
    }


    public String uploadFile(MultipartFile file, String directoryName, String fileName) throws IOException {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("folder", directoryName);
            params.put("public_id", fileName);
            params.put("resource_type", "auto");

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new IOException("Failed to upload file " + fileName + " to directory: " + directoryName, e);
        }
    }
}
