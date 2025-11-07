package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.common.UserDTO;
import dev.tomas.dma.dto.request.AddUserToCompanyReq;
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
    private final UserMapper  userMapper;
    private final UserRepo userRepo;
    private final CompanyRepo companyRepo;
    private final CompanyEmployeeRepo  companyEmployeeRepo;

    public ResponseEntity<List<UserDTO>> getEmployeesByCompany(Integer companyId) {
        List<User> entityList = new ArrayList<>(companyEmployeeRepo.findAllUsersByCompanyId(companyId));

        List<UserDTO> dtoList = new ArrayList<>();
        for (User user : entityList) {
            dtoList.add(userMapper.toDTO(user));
        }
        return ResponseEntity.ok(dtoList);
    }

    @Transactional
    public ResponseEntity<UserDTO> addUserToCompany(@Valid @RequestBody AddUserToCompanyReq request){
        User user = userRepo.findById(request.getUserId()).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.getUserId()));
        Company company = companyRepo.getReferenceById(request.getCompanyId());
        CompanyRole role = companyRoleRepo.getReferenceById(request.getRoleId());

        user.setCompany(company);
        user.setCompanyRole(role);
        userRepo.save(user);
        return ResponseEntity.ok(userMapper.toDTO(user));
    }
}
