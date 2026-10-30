package com.atlasrh.service.impl;

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
import com.atlasrh.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveRequestMapper leaveRequestMapper;

    @Override
    public LeaveRequestResponseDTO createLeaveRequest(LeaveRequestCreateDTO dto) {
        // Règle 4 : La date de fin ne peut pas être avant la date de début
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BusinessException("La date de fin ne peut pas être antérieure à la date de début.");
        }

        Employe employee = employeeRepository.findById(dto.getEmployeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'ID : " + dto.getEmployeId()));

        LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Type de congé non trouvé avec l'ID : " + dto.getLeaveTypeId()));

        // Règle 4 : Deux demandes d'un même employé ne peuvent pas se chevaucher
        if (leaveRequestRepository.existsOverlappingRequest(dto.getEmployeId(), dto.getStartDate(), dto.getEndDate())) {
            throw new BusinessException("Cette période chevauche une autre demande de congé existante.");
        }

        LeaveRequest leaveRequest = leaveRequestMapper.toEntity(dto);
        leaveRequest.setEmploye(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStatus(RequestStatus.EN_ATTENTE); // Statut initial

        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);

        LeaveRequestResponseDTO response = leaveRequestMapper.toDto(savedRequest);
        response.setWorkingDaysCount(calculateWorkingDays(savedRequest.getStartDate(), savedRequest.getEndDate()));
        return response;
    }

    @Override
    public LeaveRequestResponseDTO cancelLeaveRequest(Long requestId, Long employeeId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de congé non trouvée avec l'ID : " + requestId));

        // Vérification de la propriété de la demande
        if (!leaveRequest.getEmploye().getId().equals(employeeId)) {
            throw new BusinessException("Seul l'employé concerné peut annuler sa propre demande de congé.");
        }

        // Règle 6 : Une annulation n'est possible que si la demande est EN_ATTENTE
        if (leaveRequest.getStatus() != RequestStatus.EN_ATTENTE) {
            throw new BusinessException("Seule une demande au statut 'EN_ATTENTE' peut être annulée.");
        }

        leaveRequest.setStatus(RequestStatus.ANNULEE);
        LeaveRequest updatedRequest = leaveRequestRepository.save(leaveRequest);

        LeaveRequestResponseDTO response = leaveRequestMapper.toDto(updatedRequest);
        response.setWorkingDaysCount(calculateWorkingDays(updatedRequest.getStartDate(), updatedRequest.getEndDate()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponseDTO> getEmployeRequests(Long employeeId, RequestStatus status) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employé non trouvé avec l'ID : " + employeeId);
        }

        List<LeaveRequest> list = (status != null)
                ? leaveRequestRepository.findByEmployeIdAndStatus(employeeId, status)
                : leaveRequestRepository.findByEmployeId(employeeId);

        return list.stream().map(req -> {
            LeaveRequestResponseDTO dto = leaveRequestMapper.toDto(req);
            dto.setWorkingDaysCount(calculateWorkingDays(req.getStartDate(), req.getEndDate()));
            return dto;
        }).toList();
    }

    // Algorithme de calcul des jours ouvrés (du lundi au vendredi inclus)
    private long calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        long count = 0;
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                count++;
            }
            date = date.plusDays(1);
        }
        return count;
    }





    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponseDTO> getTeamLeaveRequests(Long managerId, RequestStatus status) {
        if (!employeeRepository.existsById(managerId)) {
            throw new ResourceNotFoundException("Manager non trouvé avec l'ID : " + managerId);
        }

        List<LeaveRequest> list = leaveRequestRepository.findTeamRequestsByManagerAndStatus(managerId, status);

        return list.stream().map(req -> {
            LeaveRequestResponseDTO dto = leaveRequestMapper.toDto(req);
            dto.setWorkingDaysCount(calculateWorkingDays(req.getStartDate(), req.getEndDate()));
            return dto;
        }).toList();
    }

    @Override
    public LeaveRequestResponseDTO processLeaveRequestDecision(Long requestId, LeaveRequestDecisionDTO dto) {
        // 1. Récupération de la demande
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de congé non trouvée avec l'ID : " + requestId));

        // 2. Vérification du statut actuel
        if (leaveRequest.getStatus() != RequestStatus.EN_ATTENTE) {
            throw new BusinessException("Seules les demandes au statut 'EN_ATTENTE' peuvent être validées ou refusées.");
        }

        // 3. Contrôle d'accès : Seul le manager direct de l'employé peut statuer
        // Remarque : On utilise getEmploye() car l'entité s'appelle Employe
        Employe employe = leaveRequest.getEmploye();
        if (employe.getManager() == null || !employe.getManager().getId().equals(dto.getManagerId())) {
            throw new BusinessException("Vous n'êtes pas autorisé à statuer sur cette demande. Seul le manager direct le peut.");
        }

        // 4. Validation du statut cible
        if (dto.getStatus() != RequestStatus.APPROUVEE && dto.getStatus() != RequestStatus.REFUSEE) {
            throw new BusinessException("Le statut de décision doit être soit 'APPROUVEE' soit 'REFUSEE'.");
        }

        // 5. Règle métier : Commentaire obligatoire en cas de refus
        if (dto.getStatus() == RequestStatus.REFUSEE && (dto.getComment() == null || dto.getComment().trim().isEmpty())) {
            throw new BusinessException("Un commentaire est obligatoire en cas de refus d'une demande de congé.");
        }

        // 6. Règle métier : Si APPROUVEE, vérifier le solde restant de l'employé
        if (dto.getStatus() == RequestStatus.APPROUVEE) {
            long requestedDays = calculateWorkingDays(leaveRequest.getStartDate(), leaveRequest.getEndDate());

            int currentYear = leaveRequest.getStartDate().getYear();
            List<LeaveRequest> approvedRequests = leaveRequestRepository
                    .findApprovedRequestsByEmployeeAndTypeAndYear(employe.getId(), leaveRequest.getLeaveType().getId(), currentYear);

            long usedDays = approvedRequests.stream()
                    .mapToLong(req -> calculateWorkingDays(req.getStartDate(), req.getEndDate()))
                    .sum();

            long remainingDays = leaveRequest.getLeaveType().getDaysPerYear() - usedDays;

            if (requestedDays > remainingDays) {
                throw new BusinessException("Solde insuffisant. Jours demandés : " + requestedDays + ", Solde restant : " + remainingDays);
            }
        }

        // 7. Enregistrement de la décision
        leaveRequest.setStatus(dto.getStatus());
        leaveRequest.setComment(dto.getComment());

        LeaveRequest updatedRequest = leaveRequestRepository.save(leaveRequest);

        LeaveRequestResponseDTO response = leaveRequestMapper.toDto(updatedRequest);
        response.setWorkingDaysCount(calculateWorkingDays(updatedRequest.getStartDate(), updatedRequest.getEndDate()));
        return response;
    }



    @Transactional(readOnly = true)
    @Override
    public EmployeeLeaveSummaryDTO getEmployeeLeaveSummary(Long employeeId) {
        Employe employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'ID : " + employeeId));

        // 1. Calcul de l'ancienneté en années et mois (US6)
        Period period = Period.between(employee.getHireDate(), LocalDate.now());
        int seniorityYears = period.getYears();
        int seniorityMonths = period.getMonths();

        // 2. Calcul du solde par type de congé (Règle 5)
        List<LeaveType> leaveTypes = leaveTypeRepository.findAll();
        int currentYear = LocalDate.now().getYear();

        List<LeaveTypeBalanceDTO> balances = leaveTypes.stream().map(leaveType -> {
            // Récupérer les demandes approuvées de l'année
            List<LeaveRequest> approvedRequests = leaveRequestRepository
                    .findApprovedRequestsByEmployeeAndTypeAndYear(employeeId, leaveType.getId(), currentYear);

            // Sommer le nombre de jours ouvrés consommés
            long usedDays = approvedRequests.stream()
                    .mapToLong(req -> calculateWorkingDays(req.getStartDate(), req.getEndDate()))
                    .sum();

            long remainingDays = leaveType.getDaysPerYear() - usedDays;

            return LeaveTypeBalanceDTO.builder()
                    .leaveTypeId(leaveType.getId())
                    .leaveTypeName(leaveType.getName())
                    .maxDaysPerYear(leaveType.getDaysPerYear())
                    .usedDays(usedDays)
                    .remainingDays(Math.max(0, remainingDays))
                    .build();
        }).toList();

        return EmployeeLeaveSummaryDTO.builder()
                .employeeId(employee.getId())
                .employeeFullName(employee.getFirstName() + " " + employee.getLastName())
                .seniorityYears(seniorityYears)
                .seniorityMonths(seniorityMonths)
                .balances(balances)
                .build();
    }





}