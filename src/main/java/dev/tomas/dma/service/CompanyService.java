package dev.tomas.dma.service;

import dev.tomas.dma.dto.common.CompanyDTO;
import dev.tomas.dma.dto.common.CompanyTypeDTO;
import dev.tomas.dma.dto.request.AddUserToCompanyReq;
import dev.tomas.dma.dto.request.CompanyCreateReq;
import dev.tomas.dma.dto.request.CompanyTypeCreateReq;
import dev.tomas.dma.dto.response.*;
import dev.tomas.dma.entity.Company;
import dev.tomas.dma.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface CompanyService {
    CompanyGetAllRes getAll();

    CompanyDTO getById(Integer id);

    CompanyDTO save(CompanyCreateReq request);

    CompanyTypeGetAllRes getAllTypes();

    CompanyTypeGetRes getTypeById(Integer id);

    CompanyTypeGetRes saveType(CompanyTypeCreateReq request);

    CompanyTypeDTO updateType(CompanyTypeDTO request);

    Integer deleteType(Integer id);

    // Internal methods
    Company findCompanyEntityById(Integer id);

}
