package dev.tomas.dma.dto.request;

import dev.tomas.dma.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FundRequestCreateReq {
    private String message;
    private BigDecimal amount;
    private Integer companyId;
}
