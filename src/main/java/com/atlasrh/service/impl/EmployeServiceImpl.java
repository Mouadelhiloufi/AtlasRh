package com.atlasrh.service.impl;

import com.atlasrh.dto.employe.EmployeRequestDTO;
import com.atlasrh.dto.employe.EmployeResponseDTO;
import com.atlasrh.entity.Department;
import com.atlasrh.entity.Employe;
import com.atlasrh.exception.BusinessException;
import com.atlasrh.exception.ResourceNotFoundException;
import com.atlasrh.mapper.EmployeMapper;
import com.atlasrh.repository.DepartmentRepository;
import com.atlasrh.repository.EmployeRepository;
import com.atlasrh.service.EmployeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeServiceImpl implements EmployeService {

    private final EmployeRepository employeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeMapper employeMapper;

    @Override
    public EmployeResponseDTO createEmploye(EmployeRequestDTO dto) {
        // Règle 1 : Unicité du matricule et de l'email
        if (employeRepository.existsByMatricule(dto.getMatricule())) {
            throw new BusinessException("Un employé avec le matricule '" + dto.getMatricule() + "' existe déjà.");
        }
        if (employeRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new BusinessException("Un employé avec l'email '" + dto.getEmail() + "' existe déjà.");
        }

        Employe employe = employeMapper.toEntity(dto);

        // Association Département
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Département non trouvé avec l'ID : " + dto.getDepartmentId()));
            employe.setDepartment(department);
        }

        // Association Manager
        if (dto.getManagerId() != null) {
            Employe manager = employeRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager non trouvé avec l'ID : " + dto.getManagerId()));
            employe.setManager(manager);
        }

        Employe savedEmploye = employeRepository.save(employe);
        return employeMapper.toDto(savedEmploye);
    }

    @Override
    public EmployeResponseDTO updateEmploye(Long id, EmployeRequestDTO dto) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'ID : " + id));

        // Règle : Unicité de l'email
        if (employeRepository.existsByEmailIgnoreCaseAndIdNot(dto.getEmail(), id)) {
            throw new BusinessException("Un autre employé utilise déjà l'email '" + dto.getEmail() + "'.");
        }

        // Règle 2 : Un employé ne peut pas être son propre manager
        if (dto.getManagerId() != null && dto.getManagerId().equals(id)) {
            throw new BusinessException("Un employé ne peut pas être son propre manager.");
        }

        employeMapper.updateEntityFromDto(dto, employe);

        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Département non trouvé avec l'ID : " + dto.getDepartmentId()));
            employe.setDepartment(department);
        } else {
            employe.setDepartment(null);
        }

        if (dto.getManagerId() != null) {
            Employe manager = employeRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager non trouvé avec l'ID : " + dto.getManagerId()));
            employe.setManager(manager);
        } else {
            employe.setManager(null);
        }

        Employe updatedEmploye = employeRepository.save(employe);
        return employeMapper.toDto(updatedEmploye);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeResponseDTO getEmployeById(Long id) {
        Employe employe = employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'ID : " + id));
        return employeMapper.toDto(employe);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeResponseDTO> getAllEmployes(String search, Long departmentId, Pageable pageable) {
        return employeRepository.searchEmployes(search, departmentId, pageable)
                .map(employeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeResponseDTO> getManagerTeam(Long managerId) {
        if (!employeRepository.existsById(managerId)) {
            throw new ResourceNotFoundException("Manager non trouvé avec l'ID : " + managerId);
        }
        return employeMapper.toDtoList(employeRepository.findByManagerId(managerId));
    }
}