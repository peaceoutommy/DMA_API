package dev.tomas.dma.entity;

import dev.tomas.dma.enums.PermissionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "company_role_permission")
public class CompanyRolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;
    @Column
    @Size(min = 3, max = 100)
    private String name;
    @Column
    private PermissionType type;
    @Column
    @Size(max = 500)
    private String description;
}
