package dev.tomas.dma.repository;

import dev.tomas.dma.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Integer> {
    List<Donation> findAllByUserId(Integer userId);
}
