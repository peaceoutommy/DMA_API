package dev.tomas.dma.dto.request;

import dev.tomas.dma.enums.EntityType;
import dev.tomas.dma.enums.TicketStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TicketSaveReq {
    private String name;
    @Enumerated(EnumType.STRING)
    private EntityType type;
    private Integer entityId;
    @Enumerated(EnumType.STRING)
    private TicketStatus status;
    private String message;
    private LocalDateTime closeDate;
    private LocalDateTime createDate;
}
