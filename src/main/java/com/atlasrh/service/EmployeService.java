package com.atlasrh.service;

import com.atlasrh.dto.employe.EmployeRequestDTO;
import com.atlasrh.dto.employe.EmployeResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeService {

    EmployeResponseDTO createEmploye(EmployeRequestDTO dto);

    EmployeResponseDTO updateEmploye(Long id, EmployeRequestDTO dto);

    EmployeResponseDTO getEmployeById(Long id);

    Page<EmployeResponseDTO> getAllEmployes(String search, Long departmentId, Pageable pageable);

    List<EmployeResponseDTO> getManagerTeam(Long managerId);
}