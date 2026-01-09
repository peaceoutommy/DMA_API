package dev.tomas.dma.dto.common;

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
public class FundRequestDTO {
    private Long id;
    private String message;
    private BigDecimal amount;
    private Status status;
    private Integer companyId;
}
