package dev.tomas.dma.dto.response;

import dev.tomas.dma.dto.common.CompanyDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CompanyGetAllRes {
    List<CompanyDTO> companies = new ArrayList<>();
}
