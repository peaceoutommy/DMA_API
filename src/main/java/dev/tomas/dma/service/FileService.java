package dev.tomas.dma.service;

import dev.tomas.dma.entity.AppFile;
import dev.tomas.dma.enums.EntityType;
import dev.tomas.dma.enums.FileType;

public interface FileService {
    AppFile saveFile(Integer entityId, String imgUrl, FileType fileType, EntityType entityType);
}
