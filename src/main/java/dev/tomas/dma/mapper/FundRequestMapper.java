package dev.tomas.dma.mapper;

import dev.tomas.dma.dto.common.FundRequestDTO;
import dev.tomas.dma.dto.request.FundRequestCreateReq;
import dev.tomas.dma.entity.FundRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FundRequestMapper {
    FundRequestDTO toDTO(FundRequest entity);
    FundRequest toEntity(FundRequestDTO dto);
}
