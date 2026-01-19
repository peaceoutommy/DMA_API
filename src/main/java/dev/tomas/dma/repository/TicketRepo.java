package dev.tomas.dma.repository;

import dev.tomas.dma.entity.Ticket;
import dev.tomas.dma.enums.EntityType;
import dev.tomas.dma.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepo extends JpaRepository<Ticket, Long> {
    List<Ticket> findAllByStatus(Status status);

    boolean existsByEntityIdAndTypeAndStatus(Integer entityId, EntityType type, Status status);

    boolean existsByEntityIdAndTypeAndStatusIn(Integer entityId, EntityType entityType, List<Status> statuses);
}
