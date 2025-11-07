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
@Table(name = "companyType")
public class CompanyType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer id;
    @Column
    @Size(min = 3, max = 100)
    private String name;

    @Column
    @Size(max = 500)
    private String description;

    @OneToMany(mappedBy = "type", orphanRemoval = true)
    private List<Company> companies = new ArrayList<>();
}