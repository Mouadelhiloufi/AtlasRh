package com.atlasrh.dto.leaverequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLeaveSummaryDTO {

    private Long employeeId;
    private String employeeFullName;
    private int seniorityYears;
    private int seniorityMonths;
    private List<LeaveTypeBalanceDTO> balances;
}