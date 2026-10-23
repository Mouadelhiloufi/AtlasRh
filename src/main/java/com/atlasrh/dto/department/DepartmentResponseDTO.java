package com.atlasrh.dto.department;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class DepartmentResponseDTO {
    private Long id;
    private String name;
    private String description;
    private int employeeCount;
}
