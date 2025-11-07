package dev.tomas.dma.repository;

import dev.tomas.dma.entity.CompanyRole;
import dev.tomas.dma.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRoleRepo extends JpaRepository<CompanyRole, Integer> {
    Optional<CompanyRole> findByCompanyIdAndName(Integer companyId, String name);
}
