package dev.tomas.dma.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ExternalStorageService {
    void createFolder(String name);

    String uploadFile(MultipartFile file, String directoryName, String fileName) throws IOException;
}
