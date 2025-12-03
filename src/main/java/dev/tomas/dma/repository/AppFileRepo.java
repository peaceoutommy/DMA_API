package dev.tomas.dma.repository;

import dev.tomas.dma.entity.AppFile;
import dev.tomas.dma.enums.EntityType;
import dev.tomas.dma.enums.FileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppFileRepo extends JpaRepository<AppFile, Long> {
    List<AppFile> findByEntityTypeAndEntityIdAndFileType(
            EntityType entityType,
            Integer entityId,
            FileType fileType
    );

    List<AppFile> findByEntityTypeAndEntityId(
            EntityType entityType,
            Integer entityId
    );
}
