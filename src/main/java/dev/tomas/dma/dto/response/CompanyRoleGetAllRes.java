package dev.tomas.dma.dto.response;

import dev.tomas.dma.dto.common.CompanyRoleDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CompanyRoleGetAllRes {
    private List<CompanyRoleDTO> roles = new ArrayList<>();
}
