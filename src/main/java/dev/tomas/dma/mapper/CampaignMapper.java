package dev.tomas.dma.mapper;

import dev.tomas.dma.dto.common.CampaignDTO;
import dev.tomas.dma.entity.Campaign;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CampaignMapper {
    public CampaignDTO convertToDTO(Campaign campaign);
    public Campaign convertToEntity(CampaignDTO campaign);
}