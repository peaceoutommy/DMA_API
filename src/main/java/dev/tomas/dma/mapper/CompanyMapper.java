package dev.tomas.dma.mapper;

import dev.tomas.dma.dto.common.CompanyDTO;
import dev.tomas.dma.entity.Company;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CompanyTypeMapper.class, CompanyPermissionMapper.class})
public interface CompanyMapper {
    CompanyDTO toDto(Company entity);
    List<CompanyDTO> entitiesToDTO(List<Company> entities);
    Company toEntity(CompanyDTO dto);
}
