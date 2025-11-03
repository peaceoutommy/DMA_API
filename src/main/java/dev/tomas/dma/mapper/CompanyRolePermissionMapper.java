package dev.tomas.dma.mapper;

import dev.tomas.dma.dto.common.CompanyRolePermissionDTO;
import dev.tomas.dma.entity.CompanyRolePermission;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CompanyRolePermissionMapper {
    CompanyRolePermissionDTO toDto(CompanyRolePermission entity);
    CompanyRolePermission toEntity(CompanyRolePermissionDTO dto);
}
