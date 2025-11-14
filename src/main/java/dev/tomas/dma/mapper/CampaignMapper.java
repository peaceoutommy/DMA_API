package dev.tomas.dma.mapper;

import dev.tomas.dma.dto.common.CampaignDTO;
import dev.tomas.dma.entity.Campaign;
import dev.tomas.dma.entity.CampaignImage;
import dev.tomas.dma.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {CompanyMapper.class})
public interface CampaignMapper {

    @Mapping(target = "images", source = "images", qualifiedByName = "imagesToUrls")
    @Mapping(target = "companyId", source = "company", qualifiedByName = "companyToId")
    CampaignDTO convertToDTO(Campaign campaign);

    @Named("imagesToUrls")
    default List<String> imagesToUrls(List<CampaignImage> images) {
        if (images == null) {
            return List.of();
        }
        return images.stream()
                .map(CampaignImage::getUrl)
                .collect(Collectors.toList());
    }

    @Named("companyToId")
    default Integer companyToId(Company company) {
        if (company == null) {
            return null;
        }
        return company.getId();
    }
}