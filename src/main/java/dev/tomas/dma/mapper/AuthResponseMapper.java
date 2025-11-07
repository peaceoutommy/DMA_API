package dev.tomas.dma.mapper;

import dev.tomas.dma.dto.response.AuthUserRes;
import dev.tomas.dma.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthResponseMapper {
    AuthResponseMapper INSTANCE = Mappers.getMapper(AuthResponseMapper.class);

    @Mapping(source = "company.id", target = "companyId")
    @Mapping(source = "companyRole.name", target = "companyRole")
    @Mapping(source = "role", target = "role")
    AuthUserRes convertToDTO(User user);
}