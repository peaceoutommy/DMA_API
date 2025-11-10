package dev.tomas.dma.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddUserToCompanyReq {
    @Positive
    @NotNull
    private Integer userId;
    @Positive
    @NotNull
    private Integer companyId;
    @NotBlank
    private Integer roleId;
}
