package dev.tomas.dma.service;

import dev.tomas.dma.dto.common.TicketDTO;
import dev.tomas.dma.dto.request.TicketCloseReq;
import dev.tomas.dma.dto.request.TicketSaveReq;
import dev.tomas.dma.dto.response.TicketDetailsGetRes;
import dev.tomas.dma.dto.response.TicketGetAllRes;
import dev.tomas.dma.entity.Campaign;
import dev.tomas.dma.entity.Company;

public interface TicketService {
    TicketDTO save(TicketSaveReq req);
    TicketDTO save(Company company);
    TicketDTO save(Campaign campaign);
    TicketGetAllRes getAllOpen();
    TicketGetAllRes getAll();
    TicketDetailsGetRes getByTicketId(Long ticketId);
    void closeTicket(TicketCloseReq req);
}
