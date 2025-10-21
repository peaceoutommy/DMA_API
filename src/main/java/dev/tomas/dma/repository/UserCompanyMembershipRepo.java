package dev.tomas.dma.repository;

import dev.tomas.dma.entity.UserCompanyMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCompanyMembershipRepo extends JpaRepository<UserCompanyMembership, Integer> {
    Optional<Integer> deleteByUserId(Integer userId);

    Optional<UserCompanyMembership> findByUserIdAndCompanyId(Integer userId, Integer companyId);

    Optional<UserCompanyMembership> findByUserId(Integer userId);
}
