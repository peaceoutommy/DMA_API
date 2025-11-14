package dev.tomas.dma.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MediaService {
    void createFolder(String name);

    String uploadImage(MultipartFile file, String folderName, String fileName) throws IOException;
}
