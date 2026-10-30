package com.atlasrh.controller;

import com.atlasrh.dto.leaverequest.EmployeeLeaveSummaryDTO;
import com.atlasrh.dto.leaverequest.LeaveRequestCreateDTO;
import com.atlasrh.dto.leaverequest.LeaveRequestDecisionDTO;
import com.atlasrh.dto.leaverequest.LeaveRequestResponseDTO;
import com.atlasrh.entity.enums.RequestStatus;
import com.atlasrh.service.LeaveRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
@Tag(name = "Congés - Employé", description = "API pour faire et annuler des demandes de congé (Module Employé)")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @PostMapping
    @Operation(summary = "Faire une nouvelle demande de congé")
    public ResponseEntity<LeaveRequestResponseDTO> createLeaveRequest(@Valid @RequestBody LeaveRequestCreateDTO dto) {
        LeaveRequestResponseDTO response = leaveRequestService.createLeaveRequest(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Annuler une demande de congé en attente")
    public ResponseEntity<LeaveRequestResponseDTO> cancelLeaveRequest(@PathVariable Long id,
                                                                      @RequestParam Long employeeId) {
        return ResponseEntity.ok(leaveRequestService.cancelLeaveRequest(id, employeeId));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Consulter les demandes d'un employé (avec filtre par statut optionnel)")
    public ResponseEntity<List<LeaveRequestResponseDTO>> getEmployeeRequests(
            @PathVariable Long employeeId,
            @RequestParam(required = false) RequestStatus status) {
        return ResponseEntity.ok(leaveRequestService.getEmployeRequests(employeeId, status));
    }

    @GetMapping("/employee/{employeeId}/summary")
    @Operation(summary = "Consulter le solde de congés et l'ancienneté d'un employé (US6)")
    public ResponseEntity<EmployeeLeaveSummaryDTO> getEmployeeLeaveSummary(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveRequestService.getEmployeeLeaveSummary(employeeId));
    }

    @GetMapping("/manager/{managerId}/team-requests")
    @Operation(summary = "Consulter les demandes de l'équipe d'un manager (Module Manager)")
    public ResponseEntity<List<LeaveRequestResponseDTO>> getTeamLeaveRequests(
            @PathVariable Long managerId,
            @RequestParam(required = false) RequestStatus status) {
        return ResponseEntity.ok(leaveRequestService.getTeamLeaveRequests(managerId, status));
    }

    @PatchMapping("/{id}/decision")
    @Operation(summary = "Valider ou refuser une demande de congé (Module Manager)")
    public ResponseEntity<LeaveRequestResponseDTO> processLeaveRequestDecision(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequestDecisionDTO dto) {
        return ResponseEntity.ok(leaveRequestService.processLeaveRequestDecision(id, dto));
    }
}