package dev.tomas.dma.mapper;

import dev.tomas.dma.dto.common.UserDTO;
import dev.tomas.dma.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "company.id", target = "companyId")
    @Mapping(source = "companyRole.id", target = "companyRoleId")
    @Mapping(source = "companyRole.name", target = "companyRoleName")
    @Mapping(source = "actualUsername", target = "username")
    UserDTO toDTO(User user);
    User toEntity(UserDTO userDTO);
}
