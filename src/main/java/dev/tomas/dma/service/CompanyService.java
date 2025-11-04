package dev.tomas.dma.service;

import dev.tomas.dma.dto.common.CompanyDTO;
import dev.tomas.dma.dto.common.CompanyTypeDTO;
import dev.tomas.dma.dto.request.AddUserToCompanyReq;
import dev.tomas.dma.dto.request.CompanyCreateReq;
import dev.tomas.dma.dto.request.CompanyTypeCreateReq;
import dev.tomas.dma.dto.response.*;
import dev.tomas.dma.entity.UserCompanyMembership;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

public interface CompanyService {
    ResponseEntity<CompanyGetAllRes> getAll();

    ResponseEntity<CompanyDTO> save(CompanyCreateReq request);

    ResponseEntity<CompanyTypeGetAllRes> getAllTypes();

    ResponseEntity<CompanyTypeGetRes> getTypeById(Integer id);

    ResponseEntity<CompanyTypeGetRes> saveType(CompanyTypeCreateReq request);

    ResponseEntity<CompanyTypeDTO> updateType(CompanyTypeDTO request);

    Integer deleteType(Integer id);

    ResponseEntity<AddUserToCompanyRes> addUserToCompany(@Valid @RequestBody AddUserToCompanyReq request);

}
