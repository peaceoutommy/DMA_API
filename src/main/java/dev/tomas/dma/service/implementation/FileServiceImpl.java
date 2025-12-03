package dev.tomas.dma.service.implementation;

import dev.tomas.dma.entity.AppFile;
import dev.tomas.dma.enums.EntityType;
import dev.tomas.dma.enums.FileType;
import dev.tomas.dma.repository.AppFileRepo;
import dev.tomas.dma.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final AppFileRepo fileRepo;

    public AppFile saveFile(Integer entityId, String imgUrl, FileType fileType, EntityType entityType) {
        AppFile file = new AppFile();
        file.setEntityType(entityType);
        file.setFileType(fileType);
        file.setEntityId(entityId);
        file.setUrl(imgUrl);
        return fileRepo.save(file);
    }
}
