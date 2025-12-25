package dev.tomas.dma.dto.response;

import dev.tomas.dma.dto.common.AppFileDTO;
import dev.tomas.dma.dto.common.TicketDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TicketDetailsGetRes {
    private TicketDTO ticket;
    private List<AppFileDTO> files = new ArrayList<>();
}
