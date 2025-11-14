package dev.tomas.dma.repository;

import dev.tomas.dma.entity.CampaignImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignImageRepo extends JpaRepository<CampaignImage, Integer> {
}
