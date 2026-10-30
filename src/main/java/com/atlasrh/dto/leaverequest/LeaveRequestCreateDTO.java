package com.atlasrh.dto.leaverequest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestCreateDTO {

    @NotNull(message = "L'ID de l'employé est obligatoire")
    private Long employeId;

    @NotNull(message = "L'ID du type de congé est obligatoire")
    private Long leaveTypeId;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate endDate;

    @Size(max = 255, message = "Le motif ne doit pas dépasser 255 caractères")
    private String reason;
}