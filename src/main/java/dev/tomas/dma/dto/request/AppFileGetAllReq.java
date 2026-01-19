package dev.tomas.dma.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppFileGetAllReq {
    String entityType;
    Integer entityId;
}
