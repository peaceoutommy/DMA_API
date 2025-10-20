package dev.tomas.dma.repository;

import dev.tomas.dma.entity.Campaign;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepo extends CrudRepository<Campaign, Integer> {
}