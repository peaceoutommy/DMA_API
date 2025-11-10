package dev.tomas.dma.controller;

import dev.tomas.dma.dto.common.UserDTO;
import dev.tomas.dma.dto.request.AddUserToCompanyReq;
import dev.tomas.dma.service.CompanyEmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies/membership")

public class CompanyEmployeeController {
    private final CompanyEmployeeService companyEmployeeService;

    @GetMapping("/{companyId}/employees")
    public ResponseEntity<List<UserDTO>> getEmployeesByCompany(@PathVariable Integer companyId) {
        return ResponseEntity.ok(companyEmployeeService.getEmployeesByCompany(companyId));
    }

    @PreAuthorize("hasAuthority('PERMISSION_Add employee')")
    @PostMapping
    public ResponseEntity<UserDTO> addUserToCompany(@RequestBody AddUserToCompanyReq request) {
        return ResponseEntity.ok(companyEmployeeService.addUserToCompany(request));
    }
}
