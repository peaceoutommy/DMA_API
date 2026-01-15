package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.common.UserDTO;
import dev.tomas.dma.dto.request.AddUserToCompanyReq;
import dev.tomas.dma.dto.request.RemoveUserFromCompanyReq;
import dev.tomas.dma.entity.Company;
import dev.tomas.dma.entity.CompanyRole;
import dev.tomas.dma.entity.User;
import dev.tomas.dma.mapper.UserMapper;
import dev.tomas.dma.repository.CompanyEmployeeRepo;
import dev.tomas.dma.repository.CompanyRepo;
import dev.tomas.dma.repository.CompanyRoleRepo;
import dev.tomas.dma.repository.UserRepo;
import dev.tomas.dma.service.CompanyEmployeeService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CompanyEmployeeServiceImpl implements CompanyEmployeeService {
    private final CompanyRoleRepo companyRoleRepo;
    private final UserMapper userMapper;
    private final UserRepo userRepo;
    private final CompanyRepo companyRepo;
    private final CompanyEmployeeRepo companyEmployeeRepo;

    public List<UserDTO> getEmployeesByCompany(Integer companyId) {
        List<User> entityList = new ArrayList<>(companyEmployeeRepo.findAllUsersByCompanyId(companyId));

        List<UserDTO> dtoList = new ArrayList<>();
        for (User user : entityList) {
            dtoList.add(userMapper.toDTO(user));
        }
        return dtoList;
    }

    @Transactional
    public UserDTO addUserToCompany(@Valid AddUserToCompanyReq request) {
        User user = userRepo.findById(request.getEmployeeId()).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.getEmployeeId()));
        Company company = companyRepo.findById(request.getCompanyId()).orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + request.getCompanyId()));
        CompanyRole role = companyRoleRepo.findById(request.getRoleId()).orElseThrow(() -> new EntityNotFoundException("Company role not found with id: " + request.getRoleId()));

        if (user.getCompany() != null && user.getCompany().getId().equals(request.getCompanyId())) {
            throw new IllegalStateException("The user already belongs to the company with id: " + request.getCompanyId());
        }

        user.setCompany(company);
        user.setCompanyRole(role);
        User saved = userRepo.save(user);
        return userMapper.toDTO(saved);
    }

    @Transactional
    public UserDTO removeUserFromCompany(RemoveUserFromCompanyReq req) {
        User user = userRepo.findById(req.getEmployeeId()).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + req.getEmployeeId()));
        Company company = companyRepo.findById(req.getCompanyId()).orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + req.getCompanyId()));

        if (user.getCompany() == null || !user.getCompany().getId().equals(req.getCompanyId())) {
            throw new IllegalStateException("The user doesn't belong to the company with id: " + req.getCompanyId());
        }
        user.setCompany(null);
        user.setCompanyRole(null);
        return userMapper.toDTO(userRepo.save(user));
    }
}
