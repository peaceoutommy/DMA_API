package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.common.AppFileDTO;
import dev.tomas.dma.dto.common.TicketDTO;
import dev.tomas.dma.dto.request.TicketCloseReq;
import dev.tomas.dma.dto.request.TicketSaveReq;
import dev.tomas.dma.dto.response.TicketDetailsGetRes;
import dev.tomas.dma.dto.response.TicketGetAllRes;
import dev.tomas.dma.entity.Campaign;
import dev.tomas.dma.entity.Company;
import dev.tomas.dma.entity.Ticket;
import dev.tomas.dma.enums.EntityType;
import dev.tomas.dma.enums.Status;
import dev.tomas.dma.mapper.AppFileMapper;
import dev.tomas.dma.mapper.TicketMapper;
import dev.tomas.dma.repository.AppFileRepo;
import dev.tomas.dma.repository.TicketRepo;
import dev.tomas.dma.service.TicketService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    private final TicketRepo ticketRepo;
    private final TicketMapper ticketMapper;
    private final AppFileRepo fileRepo;
    private final AppFileMapper fileMapper;

    public TicketDetailsGetRes getByTicketId(Long id) {
        TicketDetailsGetRes dto = new TicketDetailsGetRes();
        TicketDTO ticket = ticketMapper.toDTO(ticketRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + id)));
        List<AppFileDTO> files = fileMapper.entitiesToDTO(fileRepo.findByEntityTypeAndEntityId(ticket.getType(), ticket.getEntityId()));

        dto.setTicket(ticket);
        dto.setFiles(files);
        return dto;
    }

    public TicketGetAllRes getAllOpen() {
        TicketGetAllRes dto = new TicketGetAllRes();
        dto.setTickets(ticketMapper.toDTOs(ticketRepo.findAllByStatus(Status.PENDING)));
        return dto;
    }

    public TicketGetAllRes getAll(){
        TicketGetAllRes dto = new TicketGetAllRes();
        dto.setTickets(ticketMapper.toDTOs(ticketRepo.findAll()));
        return dto;
    }

    public TicketDTO save(TicketSaveReq req) {
        Ticket ticket = new Ticket();
        ticket.setName(req.getName());
        ticket.setStatus(req.getStatus());
        ticket.setType(req.getType());
        if (ticket.getMessage() != null) {
            ticket.setMessage(req.getMessage());
        }
        ticket.setEntityId(req.getEntityId());
        ticket.setCreateDate(req.getCreateDate());
        if (ticket.getCloseDate() != null) {
            ticket.setCloseDate(ticket.getCloseDate());
        }
        return ticketMapper.toDTO(ticketRepo.save(ticket));
    }

    public TicketDTO save(Company company) {
        Ticket ticket = new Ticket();
        ticket.setName(company.getName());
        ticket.setStatus(Status.PENDING);
        ticket.setType(EntityType.COMPANY);
        ticket.setEntityId(company.getId());
        ticket.setCreateDate(LocalDateTime.now());

        return ticketMapper.toDTO(ticketRepo.save(ticket));
    }

    public TicketDTO save(Campaign campaign) {
        Ticket ticket = new Ticket();
        ticket.setName(campaign.getName());
        ticket.setStatus(Status.PENDING);
        ticket.setType(EntityType.CAMPAIGN);
        ticket.setEntityId(campaign.getId());
        ticket.setCreateDate(LocalDateTime.now());

        return ticketMapper.toDTO(ticketRepo.save(ticket));
    }

    public void closeTicket(TicketCloseReq req){
        Ticket ticket = ticketRepo.findById(req.getId()).orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + req.getId()));
        ticket.setStatus(req.getStatus());
        ticket.setMessage(req.getMessage());
        ticket.setCloseDate(LocalDateTime.now());
        ticketRepo.save(ticket);
    }
}
