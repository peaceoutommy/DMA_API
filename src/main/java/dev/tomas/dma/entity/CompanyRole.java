package dev.tomas.dma.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "company_role")
public class CompanyRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;
    @Column
    @Size(min = 3, max = 100)
    private String name;

    @ManyToOne()
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @OneToMany(mappedBy = "companyRole")
    private List<UserCompanyMembership> memberships = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "company_role_permission_mapping",
            joinColumns = @JoinColumn(name = "company_role_id"),
            inverseJoinColumns = @JoinColumn(name = "company_role_permission_id")
    )
    private List<CompanyRolePermission> permissions = new ArrayList<>();
}
