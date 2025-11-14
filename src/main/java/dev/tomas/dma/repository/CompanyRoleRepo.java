package dev.tomas.dma.repository;

import dev.tomas.dma.entity.CompanyRole;
import dev.tomas.dma.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface CompanyRoleRepo extends Repository<CompanyRole, Integer>, JpaRepository<CompanyRole, Integer> {
    CompanyRole findByCompanyIdAndName(Integer companyId, String name);

    List<CompanyRole> findAllByCompanyId(Integer companyId);
}
