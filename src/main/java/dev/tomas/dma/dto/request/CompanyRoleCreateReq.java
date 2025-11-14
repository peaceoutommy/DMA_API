package dev.tomas.dma.dto.request;

import dev.tomas.dma.dto.common.CompanyPermissionDTO;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CompanyRoleCreateReq {
    @Positive(message = "Company Id must be positive")
    private Integer companyId;
    @NotBlank
    @Size(min = 1, max = 100, message = "Please provide a company role name between 1 and 50 characters")
    private String name;
    @Nullable
    private List<Integer> permissionIds = new ArrayList<>();
}
