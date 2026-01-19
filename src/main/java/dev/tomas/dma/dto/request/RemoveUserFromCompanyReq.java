package dev.tomas.dma.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RemoveUserFromCompanyReq {
    @Positive
    @NotNull
    private Integer employeeId;
    @Positive
    @NotNull
    private Integer companyId;
}
