package dev.tomas.dma.mapper;

import dev.tomas.dma.dto.common.CampaignDTO;
import dev.tomas.dma.entity.Campaign;
import dev.tomas.dma.entity.CampaignImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CampaignMapper {

    @Mapping(target = "images", source = "images", qualifiedByName = "imagesToUrls")
    public CampaignDTO convertToDTO(Campaign campaign);


    @Named("imagesToUrls")
    default List<String> imagesToUrls(List<CampaignImage> images) {
        if (images == null) {
            return List.of();
        }
        return images.stream()
                .map(CampaignImage::getUrl)
                .collect(Collectors.toList());
    }
}