package dev.tomas.dma.repository;

import dev.tomas.dma.entity.CompanyPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface CompanyRolePermissionRepo extends JpaRepository<CompanyPermission, Integer> {
}
