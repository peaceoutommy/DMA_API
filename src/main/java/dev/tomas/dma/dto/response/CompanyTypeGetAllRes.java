package dev.tomas.dma.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CompanyTypeGetAllRes {
    public List<CompanyTypeGetRes> types = new ArrayList<>();
}
