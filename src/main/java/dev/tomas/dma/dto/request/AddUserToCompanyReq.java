package dev.tomas.dma.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddUserToCompanyReq {
    @Positive
    @NotBlank
    private Integer userId;
    @Positive
    @NotBlank
    private Integer companyId;
    @NotBlank
    private Integer roleId;
}
