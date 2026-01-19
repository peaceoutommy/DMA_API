package dev.tomas.dma.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CompanyTypeGetAllRes {
    private List<CompanyTypeGetRes> companyTypes = new ArrayList<>();
}
