package com.atlasrh.dto.leaverequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveTypeBalanceDTO {

    private Long leaveTypeId;
    private String leaveTypeName;
    private int maxDaysPerYear;
    private long usedDays;
    private long remainingDays;
}