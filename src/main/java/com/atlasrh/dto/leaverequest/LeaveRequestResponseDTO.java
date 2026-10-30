package com.atlasrh.dto.leaverequest;

import com.atlasrh.entity.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestResponseDTO {

    private Long id;
    private Long employeeId;
    private String employeeFullName;
    private Long leaveTypeId;
    private String leaveTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private long workingDaysCount; // Nombre de jours ouvrés calculé
    private String reason;
    private RequestStatus status;
    private String comment;
}