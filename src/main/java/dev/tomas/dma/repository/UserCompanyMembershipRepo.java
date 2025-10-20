package dev.tomas.dma.repository;

import dev.tomas.dma.entity.UserCompanyMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCompanyMembershipRepo extends JpaRepository<UserCompanyMembership, Integer> {
    Optional<Integer> deleteByUserId(Integer userId);
    Optional<UserCompanyMembership> findByUserIdAndCompanyId(Integer userId, Integer companyId);
}
