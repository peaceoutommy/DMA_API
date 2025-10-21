package dev.tomas.dma;

import dev.tomas.dma.entity.Campaign;
import dev.tomas.dma.repository.CampaignRepo;
import dev.tomas.dma.service.implementation.CampaignServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTests {

    @Mock
    private CampaignRepo campaignRepo;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    @Test
    void testFindById_found() {
        Campaign entity = new Campaign();
        entity.setId(1);
        entity.setName("Test");
        entity.setDescription("Desc");

        when(campaignRepo.findById(1)).thenReturn(Optional.of(entity));

        var result = campaignService.findById(1);

        assertEquals("Test", result.getName());
    }

    @Test
    void testFindById_notFound() {
        when(campaignRepo.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> campaignService.findById(1));
    }

    @Test
    void testFindAll() {
        Campaign entity = new Campaign();
        entity.setId(1);
        entity.setCompanyId(1);
        entity.setName("Test name");
        entity.setDescription("Test description");
        entity.setFundGoal(BigDecimal.valueOf(10000));

        Campaign entity2 = new Campaign();
        entity2.setId(2);
        entity2.setCompanyId(1);
        entity2.setName("Test name2");
        entity2.setDescription("Test description2");
        entity2.setFundGoal(BigDecimal.valueOf(9999));

        when(campaignRepo.findAll()).thenReturn(List.of(entity,entity2));
        var result = campaignService.findAll();

        assertEquals(campaignRepo.findAll(), List.of(entity,entity2));
    }
}