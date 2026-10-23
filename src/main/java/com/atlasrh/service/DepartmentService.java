package com.atlasrh.service;

import com.atlasrh.dto.department.DepartmentRequestDTO;
import com.atlasrh.dto.department.DepartmentResponseDTO;

import java.util.List;

public interface DepartmentService {

    DepartmentResponseDTO createDepartment(DepartmentRequestDTO dto);

    DepartmentResponseDTO updateDepartment(Long id, DepartmentRequestDTO dto);

    DepartmentResponseDTO getDepartmentById(Long id);

    List<DepartmentResponseDTO> getAllDepartments();

    void deleteDepartment(Long id);
}