package dev.tomas.dma.dto.common;

import dev.tomas.dma.enums.EntityType;
import dev.tomas.dma.enums.FileType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppFileDTO {
    private Long id;
    private String url;
    private Integer entityId;
    private EntityType entityType;
    private FileType fileType;
}
