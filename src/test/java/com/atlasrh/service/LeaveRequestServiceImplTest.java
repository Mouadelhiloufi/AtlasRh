package com.atlasrh.service;

import com.atlasrh.dto.leaverequest.*;
import com.atlasrh.entity.Employe;
import com.atlasrh.entity.LeaveRequest;
import com.atlasrh.entity.LeaveType;
import com.atlasrh.entity.enums.RequestStatus;
import com.atlasrh.exception.BusinessException;
import com.atlasrh.exception.ResourceNotFoundException;
import com.atlasrh.mapper.LeaveRequestMapper;
import com.atlasrh.repository.EmployeRepository;
import com.atlasrh.repository.LeaveRequestRepository;
import com.atlasrh.repository.LeaveTypeRepository;
import com.atlasrh.service.impl.LeaveRequestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceImplTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeRepository employeeRepository;

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @Mock
    private LeaveRequestMapper leaveRequestMapper;

    @InjectMocks
    private LeaveRequestServiceImpl leaveRequestService;

    private Employe employe;
    private Employe manager;
    private LeaveType leaveType;
    private LeaveRequest leaveRequest;
    private LeaveRequestCreateDTO createDTO;
    private LeaveRequestResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        manager = new Employe();
        manager.setId(2L);
        manager.setFirstName("Manager");
        manager.setLastName("RH");

        employe = new Employe();
        employe.setId(1L);
        employe.setFirstName("Mouad");
        employe.setLastName("El Hiloufi");
        employe.setManager(manager);
        employe.setHireDate(LocalDate.now().minusYears(2));

        leaveType = new LeaveType();
        leaveType.setId(10L);
        leaveType.setName("PAYE");
        leaveType.setDaysPerYear(25);

        leaveRequest = new LeaveRequest();
        leaveRequest.setId(100L);
        leaveRequest.setEmploye(employe);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(LocalDate.of(2026, 11, 16)); // Lundi
        leaveRequest.setEndDate(LocalDate.of(2026, 11, 20));   // Vendredi (5 jours ouvrés)
        leaveRequest.setStatus(RequestStatus.EN_ATTENTE);

        createDTO = new LeaveRequestCreateDTO();
        createDTO.setEmployeId(1L);
        createDTO.setLeaveTypeId(10L);
        createDTO.setStartDate(LocalDate.of(2026, 11, 16));
        createDTO.setEndDate(LocalDate.of(2026, 11, 20));

        responseDTO = new LeaveRequestResponseDTO();
        responseDTO.setId(100L);
        responseDTO.setStatus(RequestStatus.EN_ATTENTE);
    }

    @Nested
    @DisplayName("Création de demande de congé")
    class CreateLeaveRequestTests {

        @Test
        @DisplayName("Given valid dates and balance, when create, then return response DTO with working days")
        void shouldCreateLeaveRequestSuccessfully() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employe));
            when(leaveTypeRepository.findById(10L)).thenReturn(Optional.of(leaveType));
            when(leaveRequestRepository.existsOverlappingRequest(1L, createDTO.getStartDate(), createDTO.getEndDate())).thenReturn(false);
            when(leaveRequestMapper.toEntity(createDTO)).thenReturn(leaveRequest);
            when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(leaveRequest);
            when(leaveRequestMapper.toDto(leaveRequest)).thenReturn(responseDTO);

            LeaveRequestResponseDTO result = leaveRequestService.createLeaveRequest(createDTO);

            assertNotNull(result);
            assertEquals(5, result.getWorkingDaysCount());
            verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
        }

        @Test
        @DisplayName("Given end date before start date, when create, then throw BusinessException")
        void shouldThrowExceptionWhenEndDateBeforeStartDate() {
            createDTO.setStartDate(LocalDate.of(2026, 11, 20));
            createDTO.setEndDate(LocalDate.of(2026, 11, 16));

            BusinessException ex = assertThrows(BusinessException.class, () -> leaveRequestService.createLeaveRequest(createDTO));
            assertTrue(ex.getMessage().contains("antérieure à la date de début"));
        }

        @Test
        @DisplayName("Given overlapping request, when create, then throw BusinessException")
        void shouldThrowExceptionWhenOverlapping() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employe));
            when(leaveTypeRepository.findById(10L)).thenReturn(Optional.of(leaveType));
            when(leaveRequestRepository.existsOverlappingRequest(1L, createDTO.getStartDate(), createDTO.getEndDate())).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class, () -> leaveRequestService.createLeaveRequest(createDTO));
            assertTrue(ex.getMessage().contains("chevauche une autre demande"));
        }
    }

    @Nested
    @DisplayName("Annulation de demande de congé")
    class CancelLeaveRequestTests {

        @Test
        @DisplayName("Given EN_ATTENTE status and owner employee, when cancel, then update status to ANNULEE")
        void shouldCancelLeaveRequestSuccessfully() {
            when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));
            when(leaveRequestRepository.save(leaveRequest)).thenReturn(leaveRequest);
            when(leaveRequestMapper.toDto(leaveRequest)).thenReturn(responseDTO);

            LeaveRequestResponseDTO result = leaveRequestService.cancelLeaveRequest(100L, 1L);

            assertNotNull(result);
            assertEquals(RequestStatus.ANNULEE, leaveRequest.getStatus());
            verify(leaveRequestRepository, times(1)).save(leaveRequest);
        }

        @Test
        @DisplayName("Given non-owner employee, when cancel, then throw BusinessException")
        void shouldThrowExceptionWhenNotOwner() {
            when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));

            BusinessException ex = assertThrows(BusinessException.class, () -> leaveRequestService.cancelLeaveRequest(100L, 99L));
            assertTrue(ex.getMessage().contains("Seul l'employé concerné"));
        }

        @Test
        @DisplayName("Given non-pending status, when cancel, then throw BusinessException")
        void shouldThrowExceptionWhenNotPending() {
            leaveRequest.setStatus(RequestStatus.APPROUVEE);
            when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));

            BusinessException ex = assertThrows(BusinessException.class, () -> leaveRequestService.cancelLeaveRequest(100L, 1L));
            assertTrue(ex.getMessage().contains("Seule une demande au statut 'EN_ATTENTE'"));
        }
    }

    @Nested
    @DisplayName("Décision du Manager sur la demande")
    class DecisionLeaveRequestTests {

        private LeaveRequestDecisionDTO decisionDTO;

        @BeforeEach
        void setUpDecision() {
            decisionDTO = new LeaveRequestDecisionDTO();
            decisionDTO.setManagerId(2L);
            decisionDTO.setStatus(RequestStatus.APPROUVEE);
        }

        @Test
        @DisplayName("Given valid manager and sufficient balance, when approve, then set status to APPROUVEE")
        void shouldApproveLeaveRequestSuccessfully() {
            when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));
            when(leaveRequestRepository.findApprovedRequestsByEmployeeAndTypeAndYear(eq(1L), eq(10L), anyInt()))
                    .thenReturn(Collections.emptyList());
            when(leaveRequestRepository.save(leaveRequest)).thenReturn(leaveRequest);
            when(leaveRequestMapper.toDto(leaveRequest)).thenReturn(responseDTO);

            LeaveRequestResponseDTO result = leaveRequestService.processLeaveRequestDecision(100L, decisionDTO);

            assertNotNull(result);
            assertEquals(RequestStatus.APPROUVEE, leaveRequest.getStatus());
        }

        @Test
        @DisplayName("Given insufficient leave balance, when approve, then throw BusinessException")
        void shouldThrowExceptionWhenBalanceInsufficient() {
            // Un congé déjà approuvé de 23 jours (25 - 23 = 2 restants, mais on demande 5 jours)
            LeaveRequest existingApproved = new LeaveRequest();
            existingApproved.setStartDate(LocalDate.of(2026, 1, 1));
            existingApproved.setEndDate(LocalDate.of(2026, 1, 31)); // 23 jours ouvrés

            when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));
            when(leaveRequestRepository.findApprovedRequestsByEmployeeAndTypeAndYear(eq(1L), eq(10L), anyInt()))
                    .thenReturn(List.of(existingApproved));

            BusinessException ex = assertThrows(BusinessException.class, () -> leaveRequestService.processLeaveRequestDecision(100L, decisionDTO));
            assertTrue(ex.getMessage().contains("Solde insuffisant"));
        }

        @Test
        @DisplayName("Given refusal without comment, when process decision, then throw BusinessException")
        void shouldThrowExceptionWhenRefusedWithoutComment() {
            decisionDTO.setStatus(RequestStatus.REFUSEE);
            decisionDTO.setComment("   ");

            when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));

            BusinessException ex = assertThrows(BusinessException.class, () -> leaveRequestService.processLeaveRequestDecision(100L, decisionDTO));
            assertTrue(ex.getMessage().contains("Un commentaire est obligatoire"));
        }

        @Test
        @DisplayName("Given unauthorized manager, when process decision, then throw BusinessException")
        void shouldThrowExceptionWhenManagerNotAuthorized() {
            decisionDTO.setManagerId(99L); // Mauvais manager

            when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));

            BusinessException ex = assertThrows(BusinessException.class, () -> leaveRequestService.processLeaveRequestDecision(100L, decisionDTO));
            assertTrue(ex.getMessage().contains("Seul le manager direct le peut"));
        }
    }

    @Nested
    @DisplayName("Résumé de solde et consultation")
    class SummaryTests {

        @Test
        @DisplayName("Given existing employee, when getEmployeeLeaveSummary, then return calculated seniority and balances")
        void shouldReturnEmployeeLeaveSummary() {
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employe));
            when(leaveTypeRepository.findAll()).thenReturn(List.of(leaveType));
            when(leaveRequestRepository.findApprovedRequestsByEmployeeAndTypeAndYear(eq(1L), eq(10L), anyInt()))
                    .thenReturn(Collections.emptyList());

            EmployeeLeaveSummaryDTO summary = leaveRequestService.getEmployeeLeaveSummary(1L);

            assertNotNull(summary);
            assertEquals(1L, summary.getEmployeeId());
            assertEquals(2, summary.getSeniorityYears());
            assertEquals(1, summary.getBalances().size());
            assertEquals(25, summary.getBalances().get(0).getRemainingDays());
        }

        @Test
        @DisplayName("Given non-existing employee ID, when getEmployeeLeaveSummary, then throw ResourceNotFoundException")
        void shouldThrowExceptionWhenEmployeeNotFound() {
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> leaveRequestService.getEmployeeLeaveSummary(99L));
        }
    }
}