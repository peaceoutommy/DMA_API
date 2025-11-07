package dev.tomas.dma.mapper;

import dev.tomas.dma.dto.common.CompanyRolePermissionDTO;
import dev.tomas.dma.entity.CompanyPermission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyRolePermissionMapper {
    CompanyRolePermissionDTO toDto(CompanyPermission entity);
    CompanyPermission toEntity(CompanyRolePermissionDTO dto);
}
