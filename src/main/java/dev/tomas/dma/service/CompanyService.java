package dev.tomas.dma.service;

import dev.tomas.dma.dto.common.CompanyTypeDTO;
import dev.tomas.dma.dto.request.AddUserToCompanyReq;
import dev.tomas.dma.dto.request.CompanyCreateReq;
import dev.tomas.dma.dto.request.CompanyTypeCreateReq;
import dev.tomas.dma.dto.response.*;
import dev.tomas.dma.entity.UserCompanyMembership;
import dev.tomas.dma.model.UserCompanyMembershipModel;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

public interface CompanyService {
    CompanyGetAllRes getAll();

    CompanyCreateRes save(CompanyCreateReq request);

    Optional<CompanyTypeGetAllRes> getAllTypes();

    Optional<CompanyTypeGetRes> getTypeById(Integer id);

    CompanyTypeGetRes saveType(CompanyTypeCreateReq request);

    Optional<CompanyTypeDTO> updateType(CompanyTypeDTO request);

    Integer deleteType(Integer id);

    Optional<AddUserToCompanyRes> addUserToCompany(@Valid @RequestBody AddUserToCompanyReq request);

    Optional<UserCompanyMembershipModel> getMembershipByUserId(Integer userId);
}
