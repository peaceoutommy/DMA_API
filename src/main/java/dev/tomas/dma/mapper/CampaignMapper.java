package dev.tomas.dma.mapper;

import dev.tomas.dma.dto.common.CampaignDTO;
import dev.tomas.dma.entity.Campaign;
import dev.tomas.dma.entity.AppFile;
import dev.tomas.dma.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {CompanyMapper.class})
public interface CampaignMapper {

    @Mapping(target = "companyId", source = "company", qualifiedByName = "companyToId")
    CampaignDTO convertToDTO(Campaign campaign);

    @Named("companyToId")
    default Integer companyToId(Company company) {
        if (company == null) {
            return null;
        }
        return company.getId();
    }
}