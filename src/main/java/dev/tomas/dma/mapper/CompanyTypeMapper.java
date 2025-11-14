package dev.tomas.dma.mapper;

import dev.tomas.dma.dto.common.CompanyTypeDTO;
import dev.tomas.dma.entity.CompanyType;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CompanyTypeMapper {
    CompanyTypeDTO convertToDTO(CompanyType companyType);
    CompanyType convertToEntity(CompanyTypeDTO companyType);
}
