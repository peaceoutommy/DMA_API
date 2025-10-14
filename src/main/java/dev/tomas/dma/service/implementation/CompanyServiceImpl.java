package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.*;
import dev.tomas.dma.model.entity.CompanyEntity;
import dev.tomas.dma.model.entity.CompanyTypeEntity;
import dev.tomas.dma.repository.CompanyRepo;
import dev.tomas.dma.repository.CompanyTypeRepo;
import dev.tomas.dma.service.CompanyService;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    CompanyRepo companyRepo;
    CompanyTypeRepo companyTypeRepo;
    EntityManager entityManager;

    public CompanyGetAllRes getAll() {
        CompanyGetAllRes response = new CompanyGetAllRes();
        List<CompanyDTO> dtos = new ArrayList<>();

        for (CompanyEntity entity : companyRepo.findAll()) {
            CompanyTypeEntity type = entity.getType();
            dtos.add(new CompanyDTO(entity.getId(), entity.getName(), entity.getRegistrationNumber(), entity.getTaxId(), new CompanyTypeDTO(type.getId(), type.getName())));
        }

        response.setCompanies(dtos);
        return response;
    }

    public CompanyCreateRes save(@Valid CompanyCreateReq request) {
        CompanyEntity toSave = new CompanyEntity();
        toSave.setName(request.getName());
        toSave.setRegistrationNumber(request.getRegistrationNumber());
        toSave.setTaxId(request.getTaxId());
        CompanyTypeEntity typeRef = entityManager.getReference(CompanyTypeEntity.class, request.getTypeId());
        toSave.setType(typeRef);

        CompanyEntity saved = companyRepo.save(toSave);

        CompanyTypeDTO typeDTO = new CompanyTypeDTO(saved.getType().getId(), saved.getType().getName());

        return new CompanyCreateRes(saved.getId(), saved.getName(), saved.getRegistrationNumber(), saved.getTaxId(), typeDTO);
    }

    public Optional<CompanyTypeGetAllRes> getAllTypes() {
        CompanyTypeGetAllRes response = new CompanyTypeGetAllRes();

        for (CompanyTypeEntity entity : companyTypeRepo.findAll()) {
            response.types.add(
                    new CompanyTypeGetRes(entity.getId(), entity.getName())
            );
        }
        return Optional.of(response);
    }

    public Optional<CompanyTypeGetRes> getTypeById(@Positive Integer id) {
        return companyTypeRepo.findById(id)
                .map(entity -> new CompanyTypeGetRes(entity.getId(), entity.getName()));
    }


    public CompanyTypeGetRes saveType(@Valid CompanyTypeCreateReq request) {
        if (StringUtils.isBlank(request.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company type can't be empty");
        }

        CompanyTypeEntity toSave = new CompanyTypeEntity();
        toSave.setName(request.getName());
        CompanyTypeEntity saved = companyTypeRepo.save(toSave);

        return new CompanyTypeGetRes(saved.getId(), saved.getName());
    }
}
