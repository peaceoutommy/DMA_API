package dev.tomas.dma.mapper;

import dev.tomas.dma.entity.UserCompanyMembership;
import dev.tomas.dma.model.UserCompanyMembershipModel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserCompanyMembershipMapper {
    UserCompanyMembershipMapper INSTANCE = Mappers.getMapper(UserCompanyMembershipMapper.class);

}
