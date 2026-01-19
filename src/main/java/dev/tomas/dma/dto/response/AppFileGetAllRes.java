package dev.tomas.dma.dto.response;

import dev.tomas.dma.entity.AppFile;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AppFileGetAllRes {
    List<AppFile> files = new ArrayList<>();
}
