package dev.tomas.dma.repository;

import dev.tomas.dma.entity.Campaign;
import dev.tomas.dma.enums.CampaignStatus;
import dev.tomas.dma.enums.EntityType;
import dev.tomas.dma.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignRepo extends JpaRepository<Campaign, Integer> {
    List<Campaign> findAllByCompanyId(Integer companyId);

    List<Campaign> findAllByStatus(CampaignStatus status);
    List<Campaign> findAllByStatusIn(List<CampaignStatus> statuses);

    @Query("""
    SELECT c FROM Campaign c 
    WHERE c.status IN :statuses 
    AND NOT EXISTS (
        SELECT t FROM Ticket t 
        WHERE t.entityId = c.id 
        AND t.type = :entityType 
        AND t.status IN :ticketStatuses
    )
""")
    List<Campaign> findAllByStatusInExcludingTickets(
            @Param("statuses") List<CampaignStatus> statuses,
            @Param("entityType") EntityType entityType,
            @Param("ticketStatuses") List<Status> ticketStatuses
    );
}