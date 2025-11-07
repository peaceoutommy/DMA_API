package dev.tomas.dma.repository;

import dev.tomas.dma.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepo extends JpaRepository<Campaign, Integer> {
}