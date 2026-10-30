package com.atlasrh.repository;

import com.atlasrh.entity.Employe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long> {

    boolean existsByMatricule(String matricule);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    // Recherche paginée par nom/prénom ou par nom de département
    @Query("SELECT e FROM Employe e " +
            "LEFT JOIN e.department d " +
            "WHERE (COALESCE(:searchTerm, '') = '' " +
            "   OR LOWER(e.firstName) LIKE CONCAT('%', LOWER(COALESCE(:searchTerm, '')), '%') " +
            "   OR LOWER(e.lastName) LIKE CONCAT('%', LOWER(COALESCE(:searchTerm, '')), '%')) " +
            "AND (:departmentId IS NULL OR d.id = :departmentId)")
    Page<Employe> searchEmployes(@Param("searchTerm") String searchTerm,
                                   @Param("departmentId") Long departmentId,
                                   Pageable pageable);

    // Récupérer l'équipe d'un manager
    List<Employe> findByManagerId(Long managerId);
}