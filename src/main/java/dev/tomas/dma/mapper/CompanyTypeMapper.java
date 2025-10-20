package dev.tomas.dma.mapper;

import dev.tomas.dma.dto.common.CompanyTypeDTO;
import dev.tomas.dma.entity.CompanyType;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CompanyTypeMapper {
    CompanyTypeMapper INSTANCE = Mappers.getMapper(CompanyTypeMapper.class);

    public CompanyTypeDTO convertToDTO(CompanyType companyType);
    public CompanyType convertToEntity(CompanyTypeDTO companyType);
}
