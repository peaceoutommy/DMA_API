package dev.tomas.dma.mapper;

import dev.tomas.dma.dto.common.CompanyPermissionDTO;
import dev.tomas.dma.entity.CompanyPermission;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompanyPermissionMapper {
    CompanyPermissionDTO toDto(CompanyPermission entity);
    CompanyPermission toEntity(CompanyPermissionDTO dto);

    List<CompanyPermissionDTO> toDtos(List<CompanyPermission> entity);
    List<CompanyPermission> toEntities(List<CompanyPermissionDTO> dtos);
}
