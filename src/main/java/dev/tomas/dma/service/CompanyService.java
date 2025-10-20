package dev.tomas.dma.service;

import dev.tomas.dma.dto.request.AddUserToCompanyReq;
import dev.tomas.dma.dto.request.CompanyCreateReq;
import dev.tomas.dma.dto.request.CompanyTypeCreateReq;
import dev.tomas.dma.dto.response.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

public interface CompanyService {
    CompanyGetAllRes getAll();

    CompanyCreateRes save(CompanyCreateReq request);

    Optional<CompanyTypeGetAllRes> getAllTypes();

    Optional<CompanyTypeGetRes> getTypeById(Integer id);

    CompanyTypeGetRes saveType(CompanyTypeCreateReq request);

    Optional<AddUserToCompanyRes> addUserToCompany(@Valid @RequestBody AddUserToCompanyReq request);


}
