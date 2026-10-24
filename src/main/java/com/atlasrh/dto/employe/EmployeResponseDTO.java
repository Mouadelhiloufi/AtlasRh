package com.atlasrh.dto.employe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeResponseDTO {

    private Long id;
    private String matricule;
    private String firstName;
    private String lastName;
    private String email;
    private String position;
    private LocalDate hireDate;
    private Long departmentId;
    private String departmentName;
    private Long managerId;
    private String managerFullName;
}