package dev.tomas.dma.repository;

import dev.tomas.dma.entity.FundRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FundRequestRepo extends JpaRepository<FundRequest, Long> {
}
