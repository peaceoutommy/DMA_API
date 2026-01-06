package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.common.CompanyDTO;
import dev.tomas.dma.dto.common.CompanyTypeDTO;
import dev.tomas.dma.dto.common.FundRequestDTO;
import dev.tomas.dma.dto.request.CompanyCreateReq;
import dev.tomas.dma.dto.request.CompanyTypeCreateReq;
import dev.tomas.dma.dto.request.FundRequestCreateReq;
import dev.tomas.dma.dto.response.*;
import dev.tomas.dma.entity.*;
import dev.tomas.dma.enums.CompanyStatus;
import dev.tomas.dma.enums.Status;
import dev.tomas.dma.mapper.CompanyMapper;
import dev.tomas.dma.mapper.FundRequestMapper;
import dev.tomas.dma.repository.*;
import dev.tomas.dma.service.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepo companyRepo;
    private final CompanyTypeRepo companyTypeRepo;
    private final CompanyRoleRepo companyRoleRepo;
    private final CompanyRoleService roleService;
    private final ExternalStorageService externalStorageService;
    private final UserRepo userRepo;
    private final CompanyMapper companyMapper;
    private final TicketService  ticketService;
    private final FundRequestRepo fundRequestRepo;
    private final FundRequestMapper fundRequestMapper;

    public CompanyGetAllRes getAll() {
        CompanyGetAllRes response = new CompanyGetAllRes();
        List<CompanyDTO> dtos = new ArrayList<>();

        for (Company entity : companyRepo.findAll()) {
            CompanyType type = entity.getType();
            dtos.add(new CompanyDTO(entity.getId(), entity.getName(), entity.getRegistrationNumber(), entity.getTaxId(), new CompanyTypeDTO(type.getId(), type.getName(), type.getDescription()), entity.getStatus().name()));
        }

        response.setCompanies(dtos);
        return response;
    }

    public CompanyDTO getById(Integer id) {
        return companyMapper.toDto(companyRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Company not found with id" + id)));
    }

    @Transactional
    public CompanyDTO save(@Valid CompanyCreateReq request) {
        CompanyType type = companyTypeRepo.findById(request.getTypeId()).orElseThrow(() -> new EntityNotFoundException("Type not found with id: " + request.getTypeId()));
        User user = userRepo.findById(request.getUserId()).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.getUserId()));

        Company toSave = new Company();
        toSave.setName(request.getName());
        toSave.setRegistrationNumber(request.getRegistrationNumber());
        toSave.setTaxId(request.getTaxId());
        toSave.setType(type);
        toSave.setStatus(CompanyStatus.PENDING);

        Company savedCompany = companyRepo.save(toSave);

        // Create a ticket
        ticketService.save(savedCompany);

        externalStorageService.createFolder(savedCompany.getName());

        // Create employee and owner role
        List<CompanyRole> roles = createDefaultRoles(savedCompany);

        CompanyRole ownerRole = roles.stream()
                .filter(role -> "Owner".equals(role.getName()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Role not found with name: 'Owner'"));

        user.setCompanyRole(ownerRole);
        user.setCompany(savedCompany);
        userRepo.save(user);
        return companyMapper.toDto(savedCompany);
    }

    public CompanyTypeGetAllRes getAllTypes() {
        CompanyTypeGetAllRes response = new CompanyTypeGetAllRes();

        for (CompanyType entity : companyTypeRepo.findAll()) {
            response.getTypes().add(
                    new CompanyTypeGetRes(entity.getId(), entity.getName(), entity.getDescription())
            );
        }
        return response;
    }

    public CompanyTypeGetRes getTypeById(@Positive Integer id) {
        CompanyType entity = companyTypeRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company type not found with id: " + id));
        return new CompanyTypeGetRes(entity.getId(), entity.getName(), entity.getDescription());
    }


    public CompanyTypeGetRes saveType(@Valid CompanyTypeCreateReq request) {
        if (StringUtils.isBlank(request.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company type name can't be empty");
        }
        if (StringUtils.isBlank(request.getDescription())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company type description can't be empty");
        }

        CompanyType toSave = new CompanyType();
        toSave.setName(request.getName());
        toSave.setDescription(request.getDescription());
        CompanyType saved = companyTypeRepo.save(toSave);

        return new CompanyTypeGetRes(saved.getId(), saved.getName(), saved.getDescription());
    }

    public CompanyTypeDTO updateType(@Valid CompanyTypeDTO request) {
        if (StringUtils.isBlank(request.getName())) {
            throw new IllegalArgumentException("Company type name can't be empty");
        }
        if (StringUtils.isBlank(request.getDescription())) {
            throw new IllegalArgumentException("Company type description can't be empty");
        }
        CompanyType existing = companyTypeRepo.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Company type not found with id: " + request.getId()));

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());

        CompanyType saved = companyTypeRepo.save(existing);
        return new CompanyTypeDTO(saved.getId(), saved.getName(), saved.getDescription());
    }

    public Integer deleteType(@Positive Integer id) {
        companyTypeRepo.deleteById(id);
        return id;
    }

    public Company findCompanyEntityById(Integer id) {
        return companyRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + id));
    }

    public List<CompanyRole> createDefaultRoles(Company company) {
        List<CompanyRole> roles = new ArrayList<>();

        CompanyRole employee = new CompanyRole();
        employee.setCompany(company);
        employee.setName("Employee");
        CompanyRole savedEmployeeRole = companyRoleRepo.save(employee);

        CompanyRole owner = new CompanyRole();
        owner.setCompany(company);
        owner.setName("Owner");
        owner.setPermissions(roleService.getAllPermissionsEntity());
        CompanyRole savedOwnerRole = companyRoleRepo.save(owner);

        roles.add(savedEmployeeRole);
        roles.add(savedOwnerRole);
        return roles;
    }

    public FundRequestDTO submitFundRequest(FundRequestCreateReq req){
        Company company = companyRepo.findById(req.getCompanyId()).orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + req.getCompanyId()));
        FundRequest toSave = new FundRequest();
        toSave.setAmount(req.getAmount());
        toSave.setCompany(company);
        toSave.setStatus(Status.PENDING);
        toSave.setMessage(req.getMessage());
        return fundRequestMapper.toDTO(fundRequestRepo.save(toSave));
    }
}
