package dev.tomas.dma.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
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
    private List<User> users = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "company_role_permission",
            joinColumns = @JoinColumn(name = "company_role_id"),
            inverseJoinColumns = @JoinColumn(name = "company_permission_id")
    )
    private List<CompanyPermission> permissions = new ArrayList<>();
}
