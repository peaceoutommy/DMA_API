package dev.tomas.dma.repository;

import dev.tomas.dma.entity.AppFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppFileRepo extends JpaRepository<AppFile, Long> {
}
