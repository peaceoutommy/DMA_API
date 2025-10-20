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
}