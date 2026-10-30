package com.atlasrh.repository;

import com.atlasrh.entity.LeaveRequest;
import com.atlasrh.entity.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeId(Long employeeId);

    List<LeaveRequest> findByEmployeIdAndStatus(Long employeeId, RequestStatus status);

    // Règle 4 : Vérifier si deux demandes d'un même employé se chevauchent
    @Query("SELECT COUNT(lr) > 0 FROM LeaveRequest lr " +
            "WHERE lr.employe.id = :employeeId " +
            "AND lr.status != 'ANNULEE' " +
            "AND (:startDate <= lr.endDate AND :endDate >= lr.startDate)")
    boolean existsOverlappingRequest(@Param("employeeId") Long employeeId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);


    @Query("SELECT lr FROM LeaveRequest lr " +
            "WHERE lr.employe.id = :employeeId " +
            "AND lr.leaveType.id = :leaveTypeId " +
            "AND lr.status = 'APPROUVEE' " +
            "AND YEAR(lr.startDate) = :year")
    List<LeaveRequest> findApprovedRequestsByEmployeeAndTypeAndYear(@Param("employeeId") Long employeeId,
                                                                    @Param("leaveTypeId") Long leaveTypeId,
                                                                    @Param("year") int year);


    // Récupérer les demandes des employés gérés par un manager donné
    @Query("SELECT lr FROM LeaveRequest lr " +
            "WHERE lr.employe.manager.id = :managerId " +
            "AND (:status IS NULL OR lr.status = :status)")
    List<LeaveRequest> findTeamRequestsByManagerAndStatus(@Param("managerId") Long managerId,
                                                          @Param("status") RequestStatus status);


}