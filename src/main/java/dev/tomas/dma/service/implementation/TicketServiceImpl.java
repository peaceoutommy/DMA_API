package dev.tomas.dma.service.implementation;

import dev.tomas.dma.repository.TicketRepo;
import dev.tomas.dma.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    private final TicketRepo ticketRepo;
}
