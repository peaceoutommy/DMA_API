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
import dev.tomas.dma.service.CompanyRoleService;
import dev.tomas.dma.service.CompanyService;
import jakarta.persistence.EntityManager;
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
import java.util.Optional;

@Service
@AllArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRoleService companyRoleService;
    private final CompanyRepo companyRepo;
    private final CompanyTypeRepo companyTypeRepo;
    private final UserCompanyMembershipRepo membershipRepo;
    private final CompanyRoleServiceImpl roleService;
    private final EntityManager entityManager;
    private final CompanyMapper companyMapper;


    public CompanyGetAllRes getAll() {
        CompanyGetAllRes response = new CompanyGetAllRes();
        List<CompanyDTO> dtos = new ArrayList<>();

        for (Company entity : companyRepo.findAll()) {
            CompanyType type = entity.getType();
            dtos.add(new CompanyDTO(entity.getId(), entity.getName(), entity.getRegistrationNumber(), entity.getTaxId(), new CompanyTypeDTO(type.getId(), type.getName(), type.getDescription())));
        }

        response.setCompanies(dtos);
        return response;
    }

    public CompanyDTO save(@Valid CompanyCreateReq request) {
        Company companyToSave = new Company();
        companyToSave.setName(request.getName());
        companyToSave.setRegistrationNumber(request.getRegistrationNumber());
        companyToSave.setTaxId(request.getTaxId());
        CompanyType type = new CompanyType();
        type.setId(request.getTypeId());
        companyToSave.setType(type);

        Company company = createCompany(companyToSave, request.getUserId());
        return companyMapper.toDto(company);
        // create company
        // create role owner
        // add permissions to role owner
        // link role with user
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

    public Optional<CompanyTypeGetAllRes> getAllTypes() {
        CompanyTypeGetAllRes response = new CompanyTypeGetAllRes();

        for (CompanyType entity : companyTypeRepo.findAll()) {
            response.getTypes().add(
                    new CompanyTypeGetRes(entity.getId(), entity.getName(), entity.getDescription())
            );
        }
        return Optional.of(response);
    }

    public Optional<CompanyTypeGetRes> getTypeById(@Positive Integer id) {
        return companyTypeRepo.findById(id)
                .map(entity -> new CompanyTypeGetRes(entity.getId(), entity.getName(), entity.getDescription()));
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

    public Optional<CompanyTypeDTO> updateType(@Valid CompanyTypeDTO request) {
        if (StringUtils.isBlank(request.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company type name can't be empty");
        }
        if (StringUtils.isBlank(request.getDescription())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company type description can't be empty");
        }
        CompanyType existing = companyTypeRepo.findById(request.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company type not found"));

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());

        CompanyType saved = companyTypeRepo.save(existing);
        return Optional.of(new CompanyTypeDTO(saved.getId(), saved.getName(), saved.getDescription()));
    }

    public Integer deleteType(@Positive Integer id) {
        companyTypeRepo.deleteById(id);
        return id;
    }

    @Transactional
    public Optional<AddUserToCompanyRes> addUserToCompany(@Valid AddUserToCompanyReq request) {
        return null;
    }

    public Optional<UserCompanyMembership> getMembershipByUserId(Integer id) {
        return membershipRepo.findByUserId(id);
    }
}
