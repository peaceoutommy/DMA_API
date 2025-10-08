package dev.tomas.dma.repository;

import dev.tomas.dma.model.entity.CompanyEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepo extends CrudRepository<CompanyEntity, Integer> {
}
