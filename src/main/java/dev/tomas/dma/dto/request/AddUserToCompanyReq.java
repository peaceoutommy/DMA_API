package dev.tomas.dma.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddUserToCompanyReq {
    @Positive
    private Integer userId;
    @Positive
    private Integer toAddUserId;
    @Positive
    private Integer companyId;
    @NotBlank
    private String role;
}
