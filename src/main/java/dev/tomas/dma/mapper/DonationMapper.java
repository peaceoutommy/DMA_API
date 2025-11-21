package dev.tomas.dma.mapper;

import dev.tomas.dma.dto.common.DonationDTO;
import dev.tomas.dma.entity.Donation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DonationMapper {
    @Mapping(source = "campaign.id", target = "campaignId")
    @Mapping(source = "user.id", target = "userId")
    DonationDTO toDTO(Donation entity);

    @Mapping(source = "campaignId", target = "campaign.id")
    @Mapping(source = "userId", target = "user.id")
    Donation toEntity(DonationDTO dto);
}
