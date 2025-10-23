package dev.tomas.dma.mapper;

import dev.tomas.dma.dto.response.AuthUserRes;
import dev.tomas.dma.entity.User;
import dev.tomas.dma.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthResponseMapper {
    public AuthResponseMapper INSTANCE = Mappers.getMapper(AuthResponseMapper.class);

    public AuthUserRes convertToDTO(UserModel user);
}
