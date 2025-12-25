package dev.tomas.dma.controller;

import dev.tomas.dma.dto.common.TicketDTO;
import dev.tomas.dma.dto.response.TicketDetailsGetRes;
import dev.tomas.dma.dto.response.TicketGetAllRes;
import dev.tomas.dma.service.TicketService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/tickets")
public class TicketController {
    private TicketService ticketService;

    @GetMapping()
    public ResponseEntity<TicketGetAllRes> getAllOpenTickets() {
        return ResponseEntity.ok(ticketService.getAllOpen());
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDetailsGetRes> getByTicketId(@PathVariable Integer ticketId) {
        return ResponseEntity.ok(ticketService.getByTicketId(Long.valueOf(ticketId)));
    }
}
