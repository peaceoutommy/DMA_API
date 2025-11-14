package dev.tomas.dma.controller;

import dev.tomas.dma.dto.request.CampaignCreateReq;
import dev.tomas.dma.dto.response.CampaignGetAllRes;
import dev.tomas.dma.dto.request.CampaignUpdateReq;
import dev.tomas.dma.dto.common.CampaignDTO;
import dev.tomas.dma.service.CampaignService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@RestController
@AllArgsConstructor
@RequestMapping("/api/campaigns")
public class CampaignController {
    CampaignService campaignService;

    @GetMapping()
    public ResponseEntity<CampaignGetAllRes> getAll() {
        return ResponseEntity.ok(campaignService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignDTO> getById(@PathVariable Integer id) {

        return ResponseEntity.ok(campaignService.findById(id));
    }

    @PreAuthorize("hasAuthority('PERMISSION_Create campaign')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CampaignDTO> create(@Valid @ModelAttribute CampaignCreateReq request) {
        return ResponseEntity.ok(campaignService.save(request));
    }

    @PreAuthorize("hasAuthority('PERMISSION_Update campaign')")
    @PutMapping()
    public ResponseEntity<CampaignDTO> save(@RequestBody @Valid CampaignUpdateReq request) {
        return ResponseEntity.ok(campaignService.update(request));
    }

    @PreAuthorize("hasAuthority('PERMISSION_Archive campaign')")
    @PostMapping("/archive/{id}")
    public ResponseEntity<CampaignDTO> archive(@PathVariable Integer id) {
        return ResponseEntity.ok(campaignService.archive(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Integer> delete(@Positive @PathVariable Integer id) {
        if (Objects.isNull(id)) {
            throw new IllegalArgumentException("Campaign id cannot be null");
        }
        return ResponseEntity.ok(campaignService.deleteById(id));
    }
}
