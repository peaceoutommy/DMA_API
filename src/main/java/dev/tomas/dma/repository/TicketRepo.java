package dev.tomas.dma.repository;

import dev.tomas.dma.entity.Ticket;
import dev.tomas.dma.enums.EntityType;
import dev.tomas.dma.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepo extends JpaRepository<Ticket, Long> {
    List<Ticket> findAllByStatus(TicketStatus status);
    boolean existsByEntityIdAndTypeAndStatus(Integer entityId, EntityType type, TicketStatus status);
}
