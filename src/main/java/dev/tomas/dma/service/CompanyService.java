package dev.tomas.dma.service;

import dev.tomas.dma.dto.*;

import java.util.Optional;

public interface CompanyService {
    Optional<CompanyTypeGetAllRes> getAllTypes();

    Optional<CompanyTypeGetRes> getTypeById(Integer id);

    CompanyTypeGetRes saveType(CompanyTypeCreateReq request);

    CompanyCreateRes saveCompany(CompanyCreateReq request);

}
