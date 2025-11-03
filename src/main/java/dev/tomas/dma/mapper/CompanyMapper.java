package dev.tomas.dma.mapper;

import dev.tomas.dma.dto.common.CompanyDTO;
import dev.tomas.dma.entity.Company;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CompanyTypeMapper.class, CompanyRolePermissionMapper.class})
public interface CompanyMapper {
    CompanyDTO toDto(Company entity);
    Company toEntity(CompanyDTO dto);
}
