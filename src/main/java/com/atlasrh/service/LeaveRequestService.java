package com.atlasrh.service;

import com.atlasrh.dto.leaverequest.EmployeeLeaveSummaryDTO;
import com.atlasrh.dto.leaverequest.LeaveRequestCreateDTO;
import com.atlasrh.dto.leaverequest.LeaveRequestDecisionDTO;
import com.atlasrh.dto.leaverequest.LeaveRequestResponseDTO;
import com.atlasrh.entity.enums.RequestStatus; // Checkez bien cet import !
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface LeaveRequestService {

    LeaveRequestResponseDTO createLeaveRequest(LeaveRequestCreateDTO dto);

    LeaveRequestResponseDTO cancelLeaveRequest(Long requestId, Long employeeId);

    List<LeaveRequestResponseDTO> getEmployeRequests(Long employeeId, RequestStatus status);

    @Transactional(readOnly = true)
    EmployeeLeaveSummaryDTO getEmployeeLeaveSummary(Long employeeId);

    List<LeaveRequestResponseDTO> getTeamLeaveRequests(Long managerId, RequestStatus status);

    LeaveRequestResponseDTO processLeaveRequestDecision(Long requestId, LeaveRequestDecisionDTO dto);
}