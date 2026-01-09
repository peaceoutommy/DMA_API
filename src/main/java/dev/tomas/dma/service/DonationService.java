package dev.tomas.dma.service;

import dev.tomas.dma.dto.common.DonationDTO;
import dev.tomas.dma.dto.response.DonationByUserGetAllRes;

import java.util.List;
import java.util.Map;

public interface DonationService {
    void save(DonationDTO dto);
    List<DonationByUserGetAllRes> getAllByUserId(Integer userId);
}
