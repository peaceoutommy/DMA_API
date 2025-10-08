package dev.tomas.dma.mapper;

import dev.tomas.dma.model.CompanyType;
import dev.tomas.dma.model.entity.CompanyTypeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CompanyTypeMapper {
    CompanyTypeMapper INSTANCE = Mappers.getMapper(CompanyTypeMapper.class);

    public CompanyType  convertToModel(CompanyTypeEntity companyTypeEntity);
    public CompanyTypeEntity convertToEntity(CompanyType companyType);
}
