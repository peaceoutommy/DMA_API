package dev.tomas.dma.entity;

import dev.tomas.dma.enums.EntityType;
import dev.tomas.dma.enums.FileType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Table(name = "app_file")
public class AppFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String url;
    private Integer entityId;
    @Enumerated(EnumType.STRING)
    private EntityType entityType;
    @Enumerated(EnumType.STRING)
    private FileType fileType;
}
