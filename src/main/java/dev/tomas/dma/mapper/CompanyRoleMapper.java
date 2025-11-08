package dev.tomas.dma.mapper;

import dev.tomas.dma.dto.common.CompanyRoleDTO;
import dev.tomas.dma.entity.CompanyRole;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyRoleMapper {
    CompanyRoleDTO toDTO(CompanyRole entity);
    CompanyRole toEntity(CompanyRoleDTO entity);
}
