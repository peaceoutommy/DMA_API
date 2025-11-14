package dev.tomas.dma.service.implementation;

import com.cloudinary.Cloudinary;
import dev.tomas.dma.service.MediaService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class MediaServiceImpl implements MediaService {
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


    public String uploadImage(MultipartFile file, String folderName, String fileName) throws IOException {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("folder", folderName);
            params.put("public_id", fileName);
            params.put("resource_type", "auto");

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new IOException("Failed to upload image " + fileName + " to folder: " + folderName, e);
        }
    }
}
