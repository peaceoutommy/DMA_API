package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.CompanyTypeCreateRequestDto;
import dev.tomas.dma.dto.CompanyTypeDto;
import dev.tomas.dma.dto.CompanyTypeGetAllResponseDto;
import dev.tomas.dma.model.entity.CompanyTypeEntity;
import dev.tomas.dma.repository.CompanyRepo;
import dev.tomas.dma.repository.CompanyTypeRepo;
import dev.tomas.dma.service.CompanyService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    CompanyRepo companyRepo;
    CompanyTypeRepo companyTypeRepo;

    public Optional<CompanyTypeGetAllResponseDto> getAllTypes() {
        CompanyTypeGetAllResponseDto response = new CompanyTypeGetAllResponseDto();

        for (CompanyTypeEntity entity : companyTypeRepo.findAll()) {
            response.types.add(
                    new CompanyTypeDto(entity.getId(), entity.getType())
            );
        }
        return Optional.of(response);
    }

    public Optional<CompanyTypeDto> getTypeById(Integer id) {
        return companyTypeRepo.findById(id)
                .map(entity -> new CompanyTypeDto(entity.getId(), entity.getType()));
    }


    public CompanyTypeDto saveType(CompanyTypeCreateRequestDto request) {
        if (StringUtils.isBlank(request.getType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company type can't be empty");
        }

        CompanyTypeEntity toSave = new CompanyTypeEntity();
        toSave.setType(request.getType());
        CompanyTypeEntity createdCompanyType = companyTypeRepo.save(toSave);

        return new CompanyTypeDto(createdCompanyType.getId(), createdCompanyType.getType());
    }
}
