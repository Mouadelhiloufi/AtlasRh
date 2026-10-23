package com.atlasrh.service.impl;

import com.atlasrh.dto.department.DepartmentRequestDTO;
import com.atlasrh.dto.department.DepartmentResponseDTO;
import com.atlasrh.entity.Department;
import com.atlasrh.exception.BusinessException;
import com.atlasrh.exception.ResourceNotFoundException;
import com.atlasrh.mapper.DepartmentMapper;
import com.atlasrh.repository.DepartmentRepository;
import com.atlasrh.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    public DepartmentResponseDTO createDepartment(DepartmentRequestDTO dto) {
        // Règle 1 : Le nom d'un département doit être unique
        if (departmentRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new BusinessException("Un département avec le nom '" + dto.getName() + "' existe déjà.");
        }

        Department department = departmentMapper.toEntity(dto);
        Department savedDepartment = departmentRepository.save(department);
        return departmentMapper.toDto(savedDepartment);
    }

    @Override
    public DepartmentResponseDTO updateDepartment(Long id, DepartmentRequestDTO dto) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département non trouvé avec l'ID : " + id));

        // Règle 1 : Vérifier l'unicité du nom en excluant le département actuel
        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(dto.getName(), id)) {
            throw new BusinessException("Un autre département avec le nom '" + dto.getName() + "' existe déjà.");
        }

        departmentMapper.updateEntityFromDto(dto, department);
        Department updatedDepartment = departmentRepository.save(department);
        return departmentMapper.toDto(updatedDepartment);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponseDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département non trouvé avec l'ID : " + id));
        return departmentMapper.toDto(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponseDTO> getAllDepartments() {
        return departmentMapper.toDtoList(departmentRepository.findAll());
    }

    @Override
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département non trouvé avec l'ID : " + id));

        // Règle 2 : Un département qui contient des employés ne peut pas être supprimé
        if (department.getEmployees() != null && !department.getEmployees().isEmpty()) {
            throw new BusinessException("Impossible de supprimer le département '" + department.getName() + "' car il contient encore des employés.");
        }

        departmentRepository.delete(department);
    }
}