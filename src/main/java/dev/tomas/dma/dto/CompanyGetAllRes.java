package dev.tomas.dma.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CompanyGetAllRes {
    List<CompanyDTO> companies = new ArrayList<>();
}
