package dev.tomas.dma.mapper;

import dev.tomas.dma.dto.common.AppFileDTO;
import dev.tomas.dma.entity.AppFile;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AppFileMapper {
    AppFileDTO entityToDTO(AppFile file);
    List<AppFileDTO> entitiesToDTO(List<AppFile> files);
}
