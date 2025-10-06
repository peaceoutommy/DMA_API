package dev.tomas.dma.mapper;

import dev.tomas.dma.dto.AuthUserResponse;
import dev.tomas.dma.model.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthResponseMapper {
    public AuthResponseMapper INSTANCE = Mappers.getMapper(AuthResponseMapper.class);

    public AuthUserResponse convertToModel(UserEntity userEntity);
}
