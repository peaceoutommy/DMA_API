package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.CompanyTypeCreateRequestDto;
import dev.tomas.dma.dto.CompanyTypeCreateResponseDto;
import dev.tomas.dma.mapper.CompanyTypeMapper;
import dev.tomas.dma.model.CompanyType;
import dev.tomas.dma.model.entity.CompanyEntity;
import dev.tomas.dma.model.entity.CompanyTypeEntity;
import dev.tomas.dma.repository.CompanyRepo;
import dev.tomas.dma.repository.CompanyTypeRepo;
import dev.tomas.dma.service.CompanyService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    CompanyRepo companyRepo;
    CompanyTypeRepo companyTypeRepo;

    public CompanyTypeCreateResponseDto saveType(CompanyTypeCreateRequestDto request) {
        if (StringUtils.isBlank(request.getType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company type can't be empty");
        }

        CompanyTypeEntity toSave = new CompanyTypeEntity();
        toSave.setType(request.getType());
        CompanyTypeEntity createdCompanyType = companyTypeRepo.save(toSave);

        return new CompanyTypeCreateResponseDto(createdCompanyType.getId(), createdCompanyType.getType());
    }
}
