package dev.tomas.dma.dto.request;

import dev.tomas.dma.enums.Status;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketCloseReq {
    private Long id;
    private String message;
    private Status status;
}
