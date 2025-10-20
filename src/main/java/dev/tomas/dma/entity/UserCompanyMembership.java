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
    @JoinColumn(name = "userId", unique = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "companyId", nullable = false)
    private Company company;

    private String role;
}
