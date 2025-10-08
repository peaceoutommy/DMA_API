package dev.tomas.dma.controller;

import dev.tomas.dma.dto.CampaignCreateRequest;
import dev.tomas.dma.dto.CampaignGetAllResponseDto;
import dev.tomas.dma.dto.CampaignUpdateRequest;
import dev.tomas.dma.model.Campaign;
import dev.tomas.dma.service.CampaignService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@RestController
@AllArgsConstructor
@RequestMapping("/api/campaigns")
public class CampaignController {
    CampaignService campaignService;

    @GetMapping()
    public CampaignGetAllResponseDto getAll() {
        return campaignService.findAll();
    }

    @GetMapping("/{id}")
    public Campaign getById(@PathVariable Integer id) {
        return campaignService.findById(id);
    }

    @PostMapping
    public Campaign create(@RequestBody @Valid CampaignCreateRequest request) {
        return campaignService.save(request);
    }

    @PutMapping()
    public Campaign save(@RequestBody @Valid CampaignUpdateRequest request) {
        return campaignService.update(request);
    }

    @DeleteMapping("/{id}")
    public Integer delete(@PathVariable Integer id) {
        if (Objects.isNull(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campaign id cannot be null");
        }
        return campaignService.deleteById(id);
    }
}
