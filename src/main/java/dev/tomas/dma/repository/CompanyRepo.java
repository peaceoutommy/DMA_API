package dev.tomas.dma.repository;

import dev.tomas.dma.entity.Company;
import dev.tomas.dma.enums.CompanyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepo extends JpaRepository<Company, Integer> {
    Company getReferenceById(Integer id);
    List<Company> findCompaniesByStatus(CompanyStatus status);
    boolean existsByTypeId(Integer typeId);
}
