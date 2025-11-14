package dev.tomas.dma.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class CampaignCreateReq {
    @NotNull
    @Size(min = 3, max = 100, message = "Name must be at least 3 characters long")
    private String name;

    @NotBlank
    @Size(min = 10, max = 2000, message = "Description must be at least 10 characters long")
    private String description;

    @NotNull
    private Integer companyId;

    private LocalDate startDate;
    private LocalDate endDate;

    @NotNull
    @Min(1) @Max(999999999)
    private BigDecimal fundGoal;

    @NotNull
    private List<MultipartFile> images = new ArrayList<>();
}
