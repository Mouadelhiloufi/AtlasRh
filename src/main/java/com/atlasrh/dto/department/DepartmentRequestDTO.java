package com.atlasrh.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data

public class DepartmentRequestDTO {

    @NotBlank(message = "Le nom du département est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String name;

    @Size(max = 255, message = "La description ne doit pas dépasser 255 caractères")
    private String description;
}
