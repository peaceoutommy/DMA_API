package dev.tomas.dma.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="userCompany")
public class UserCompanyMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_role_id")
    private CompanyRole companyRole;

}
