package dev.tomas.dma.repository;

import dev.tomas.dma.entity.Company;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepo extends CrudRepository<Company, Integer> {
}
