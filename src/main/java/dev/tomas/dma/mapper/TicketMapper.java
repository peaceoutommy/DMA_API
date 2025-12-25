package dev.tomas.dma.mapper;

import dev.tomas.dma.dto.common.TicketDTO;
import dev.tomas.dma.entity.Ticket;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    TicketDTO toDTO(Ticket ticket);
    List<TicketDTO> toDTOs(List<Ticket> tickets);
    Ticket toEntity(TicketDTO dto);
    List<Ticket> toEntities(List<TicketDTO> dtos);
}
