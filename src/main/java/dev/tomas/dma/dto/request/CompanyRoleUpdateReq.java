package dev.tomas.dma.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class CompanyRoleUpdateReq {
    @NotNull(message = "Role id is required")
    @Positive
    private Integer id;
    @NotBlank
    @Size(min = 1, max = 100, message = "Please provide a company role name between 1 and 50 characters")
    private String name;
    @Nullable
    private List<Integer> permissionIds = new ArrayList<>();
    // @NotBlank
    // @Positive(message = "Please provide a valid company id")
    // private Integer companyId;
    // @NotBlank
    // @Positive(message = "Please provide a valid user id")
    // private Integer userId;
}
