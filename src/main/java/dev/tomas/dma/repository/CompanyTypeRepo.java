package dev.tomas.dma.repository;

import dev.tomas.dma.entity.CompanyType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyTypeRepo extends CrudRepository<CompanyType, Integer> {
}
