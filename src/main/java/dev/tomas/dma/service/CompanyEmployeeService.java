package dev.tomas.dma.service;

import dev.tomas.dma.dto.common.UserDTO;
import dev.tomas.dma.dto.request.AddUserToCompanyReq;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface CompanyEmployeeService {
    ResponseEntity<List<UserDTO>> getEmployeesByCompany(Integer companyId);
    ResponseEntity<UserDTO> addUserToCompany(@Valid @RequestBody AddUserToCompanyReq request);
}
