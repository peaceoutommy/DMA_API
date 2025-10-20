package dev.tomas.dma.controller;

import dev.tomas.dma.dto.request.CampaignCreateReq;
import dev.tomas.dma.dto.response.CampaignGetAllRes;
import dev.tomas.dma.dto.request.CampaignUpdateReq;
import dev.tomas.dma.dto.common.CampaignDTO;
import dev.tomas.dma.service.CampaignService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@RestController
@AllArgsConstructor
@RequestMapping("/api/campaigns")
public class CampaignController {
    CampaignService campaignService;

    @GetMapping()
    public CampaignGetAllRes getAll() {
        return campaignService.findAll();
    }

    @GetMapping("/{id}")
    public CampaignDTO getById(@PathVariable Integer id) {
        return campaignService.findById(id);
    }

    @PostMapping
    public CampaignDTO create(@RequestBody @Valid CampaignCreateReq request) {
        return campaignService.save(request);
    }

    @PutMapping()
    public CampaignDTO save(@RequestBody @Valid CampaignUpdateReq request) {
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
