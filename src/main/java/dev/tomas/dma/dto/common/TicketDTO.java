package dev.tomas.dma.dto.common;

import dev.tomas.dma.enums.EntityType;
import dev.tomas.dma.enums.TicketStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TicketDTO {
    private long Id;
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
