package dev.tomas.dma.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddUserToCompanyRes {
    private Integer id;
    private Integer userId;
    private Integer companyId;
    private String role;
}
