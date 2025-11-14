package dev.tomas.dma.repository;

import dev.tomas.dma.entity.CompanyPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyPermissionRepo extends JpaRepository<CompanyPermission, Integer> {
}
