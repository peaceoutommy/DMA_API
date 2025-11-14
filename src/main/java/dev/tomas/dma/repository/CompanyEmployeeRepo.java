package dev.tomas.dma.repository;

import dev.tomas.dma.entity.CompanyRole;
import dev.tomas.dma.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyEmployeeRepo extends JpaRepository<CompanyRole, Integer> {

    @Query("SELECT u FROM User u WHERE u.company.id = :companyId")
    List<User> findAllUsersByCompanyId(@Param("companyId") Integer companyId);
}
