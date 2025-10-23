package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.common.CompanyRoleDTO;
import dev.tomas.dma.dto.request.CompanyRoleCreateReq;
import dev.tomas.dma.dto.response.CompanyRoleCreateRes;
import dev.tomas.dma.dto.response.CompanyRoleGetAllRes;
import dev.tomas.dma.entity.CompanyRole;
import dev.tomas.dma.repository.CompanyRoleRepo;
import dev.tomas.dma.service.CompanyRoleService;
import lombok.AllArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class CompanyRoleServiceImpl implements CompanyRoleService {
    private final CompanyRoleRepo companyRoleRepo;

    public Optional<CompanyRoleGetAllRes> getAll(Integer companyId) {
        CompanyRoleGetAllRes response = new CompanyRoleGetAllRes();

        for (CompanyRole entity : companyRoleRepo.findAll()) {
            CompanyRoleDTO dto = new CompanyRoleDTO(entity.getId(), entity.getName());
            response.getRoles().add(dto);
        }
        return Optional.of(response);
    }

    public Optional<CompanyRoleCreateRes> save(CompanyRoleCreateReq request) {
        if (companyRoleRepo.findByCompanyIdAndName(request.getCompanyId(), request.getName()).isPresent()) {
            throw new DuplicateKeyException("Role with name '" + request.getName() + "' already exists for this company");
        }

        CompanyRole toSave = new CompanyRole();
        toSave.setName(request.getName());
        CompanyRole saved = companyRoleRepo.save(toSave);

        CompanyRoleCreateRes response = new CompanyRoleCreateRes();
        response.setId(saved.getId());
        response.setName(saved.getName());
        return Optional.of(response);
    }

    public Integer delete(Integer id) {
        companyRoleRepo.deleteById(id);
        return id;
    }
}
