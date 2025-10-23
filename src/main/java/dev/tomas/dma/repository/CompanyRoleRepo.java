package dev.tomas.dma.repository;

import dev.tomas.dma.entity.CompanyRole;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface CompanyRoleRepo extends CrudRepository<CompanyRole, Integer> {
    Optional<CompanyRole> findByCompanyIdAndName(Integer companyId, String name);
}
