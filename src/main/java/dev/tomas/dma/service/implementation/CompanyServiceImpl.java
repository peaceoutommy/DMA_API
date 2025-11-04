package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.common.CompanyDTO;
import dev.tomas.dma.dto.common.CompanyTypeDTO;
import dev.tomas.dma.dto.request.AddUserToCompanyReq;
import dev.tomas.dma.dto.request.CompanyCreateReq;
import dev.tomas.dma.dto.request.CompanyTypeCreateReq;
import dev.tomas.dma.dto.response.*;
import dev.tomas.dma.entity.Company;
import dev.tomas.dma.entity.CompanyRole;
import dev.tomas.dma.entity.CompanyType;
import dev.tomas.dma.entity.UserCompanyMembership;
import dev.tomas.dma.mapper.CompanyMapper;
import dev.tomas.dma.repository.CompanyRepo;
import dev.tomas.dma.repository.CompanyTypeRepo;
import dev.tomas.dma.repository.UserCompanyMembershipRepo;
import dev.tomas.dma.service.CompanyService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final UserCompanyMembershipRepo membershipRepo;
    private final CompanyRoleServiceImpl roleService;
    private final EntityManager entityManager;
    private final CompanyMapper companyMapper;


    public ResponseEntity<CompanyGetAllRes> getAll() {
        CompanyGetAllRes response = new CompanyGetAllRes();
        List<CompanyDTO> dtos = new ArrayList<>();

        for (Company entity : companyRepo.findAll()) {
            CompanyType type = entity.getType();
            dtos.add(new CompanyDTO(entity.getId(), entity.getName(), entity.getRegistrationNumber(), entity.getTaxId(), new CompanyTypeDTO(type.getId(), type.getName(), type.getDescription())));
        }

        response.setCompanies(dtos);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<CompanyDTO> save(@Valid CompanyCreateReq request) {
        Company companyToSave = new Company();
        companyToSave.setName(request.getName());
        companyToSave.setRegistrationNumber(request.getRegistrationNumber());
        companyToSave.setTaxId(request.getTaxId());
        CompanyType type = new CompanyType();
        type.setId(request.getTypeId());
        companyToSave.setType(type);

        Company company = createCompany(companyToSave, request.getUserId());
        return ResponseEntity.ok(companyMapper.toDto(company));
    }

    @Transactional
    protected Company createCompany(Company company, Integer userId) {
        if (company.getType() == null || company.getType().getId() == null) {
            throw new IllegalArgumentException("Company type ID cannot be null");
        }

        company.setType(entityManager.getReference(CompanyType.class, company.getType().getId()));
        Company savedCompany = companyRepo.save(company);

        CompanyRole role = new CompanyRole();
        role.setCompany(savedCompany);
        role.setName("Owner");
        role.setPermissions(roleService.getAllPermissionsEntity());
        roleService.saveRoleEntity(role, userId);

        return savedCompany;
    }

    public ResponseEntity<CompanyTypeGetAllRes> getAllTypes() {
        CompanyTypeGetAllRes response = new CompanyTypeGetAllRes();

        for (CompanyType entity : companyTypeRepo.findAll()) {
            response.getTypes().add(
                    new CompanyTypeGetRes(entity.getId(), entity.getName(), entity.getDescription())
            );
        }
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<CompanyTypeGetRes> getTypeById(@Positive Integer id) {
        CompanyType entity = companyTypeRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company type not found with id: " + id));
        return ResponseEntity.ok(new CompanyTypeGetRes(entity.getId(), entity.getName(), entity.getDescription()));
    }


    public ResponseEntity<CompanyTypeGetRes> saveType(@Valid CompanyTypeCreateReq request) {
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

        return ResponseEntity.ok(new CompanyTypeGetRes(saved.getId(), saved.getName(), saved.getDescription()));
    }

    public ResponseEntity<CompanyTypeDTO> updateType(@Valid CompanyTypeDTO request) {
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
        return ResponseEntity.ok(new CompanyTypeDTO(saved.getId(), saved.getName(), saved.getDescription()));
    }

    public Integer deleteType(@Positive Integer id) {
        companyTypeRepo.deleteById(id);
        return id;
    }

    @Transactional
    public ResponseEntity<AddUserToCompanyRes> addUserToCompany(@Valid AddUserToCompanyReq request) {
        return ResponseEntity.ok(null);
    }

    protected UserCompanyMembership getMembershipByUserId(Integer id) {
        return membershipRepo.findByUserId(id)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found with id: " + id));
    }

    protected Company findCompanyEntityById(Integer id) {
        return companyRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Company not found with  id: " + id));
    }
}
