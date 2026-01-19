package dev.tomas.dma.repository;

import dev.tomas.dma.entity.Campaign;
import dev.tomas.dma.enums.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignRepo extends JpaRepository<Campaign, Integer> {
    List<Campaign> findAllByCompanyId(Integer companyId);

    List<Campaign> findAllByStatus(CampaignStatus status);
    List<Campaign> findAllByStatusIn(List<CampaignStatus> statuses);
}