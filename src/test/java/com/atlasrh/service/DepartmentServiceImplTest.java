package com.atlasrh.service;

import com.atlasrh.dto.department.DepartmentRequestDTO;
import com.atlasrh.dto.department.DepartmentResponseDTO;
import com.atlasrh.entity.Department;
import com.atlasrh.entity.Employe;
import com.atlasrh.exception.BusinessException;
import com.atlasrh.exception.ResourceNotFoundException;
import com.atlasrh.mapper.DepartmentMapper;
import com.atlasrh.repository.DepartmentRepository;
import com.atlasrh.service.impl.DepartmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department department;
    private DepartmentRequestDTO requestDTO;
    private DepartmentResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("Engineering");
        department.setEmployees(new ArrayList<>());

        requestDTO = new DepartmentRequestDTO();
        requestDTO.setName("Engineering");

        responseDTO = new DepartmentResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("Engineering");
    }

    @Nested
    @DisplayName("Tests de createDepartment")
    class CreateDepartmentTests {

        @Test
        @DisplayName("Given valid DTO, when createDepartment, then save and return response DTO")
        void shouldCreateDepartmentSuccessfully() {
            when(departmentRepository.existsByNameIgnoreCase("Engineering")).thenReturn(false);
            when(departmentMapper.toEntity(requestDTO)).thenReturn(department);
            when(departmentRepository.save(any(Department.class))).thenReturn(department);
            when(departmentMapper.toDto(department)).thenReturn(responseDTO);

            DepartmentResponseDTO result = departmentService.createDepartment(requestDTO);

            assertNotNull(result);
            assertEquals("Engineering", result.getName());
            verify(departmentRepository, times(1)).save(any(Department.class));
        }

        @Test
        @DisplayName("Given existing name, when createDepartment, then throw BusinessException")
        void shouldThrowExceptionWhenNameExists() {
            when(departmentRepository.existsByNameIgnoreCase("Engineering")).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> departmentService.createDepartment(requestDTO));
            assertTrue(ex.getMessage().contains("existe déjà"));
            verify(departmentRepository, never()).save(any(Department.class));
        }
    }

    @Nested
    @DisplayName("Tests de updateDepartment")
    class UpdateDepartmentTests {

        @Test
        @DisplayName("Given valid update DTO, when updateDepartment, then return updated DTO")
        void shouldUpdateDepartmentSuccessfully() {
            Long deptId = 1L;
            when(departmentRepository.findById(deptId)).thenReturn(Optional.of(department));
            when(departmentRepository.existsByNameIgnoreCaseAndIdNot("Engineering", deptId)).thenReturn(false);
            when(departmentRepository.save(department)).thenReturn(department);
            when(departmentMapper.toDto(department)).thenReturn(responseDTO);

            DepartmentResponseDTO result = departmentService.updateDepartment(deptId, requestDTO);

            assertNotNull(result);
            verify(departmentRepository, times(1)).save(department);
        }

        @Test
        @DisplayName("Given duplicate name for another ID, when updateDepartment, then throw BusinessException")
        void shouldThrowExceptionWhenNameUsedByAnotherDepartment() {
            Long deptId = 1L;
            when(departmentRepository.findById(deptId)).thenReturn(Optional.of(department));
            when(departmentRepository.existsByNameIgnoreCaseAndIdNot("Engineering", deptId)).thenReturn(true);

            assertThrows(BusinessException.class, () -> departmentService.updateDepartment(deptId, requestDTO));
            verify(departmentRepository, never()).save(any(Department.class));
        }

        @Test
        @DisplayName("Given non-existing ID, when updateDepartment, then throw ResourceNotFoundException")
        void shouldThrowExceptionWhenDepartmentNotFoundOnUpdate() {
            when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> departmentService.updateDepartment(99L, requestDTO));
        }
    }

    @Nested
    @DisplayName("Tests de consultation (getDepartmentById & getAllDepartments)")
    class ReadDepartmentTests {

        @Test
        @DisplayName("Given existing ID, when getDepartmentById, then return DTO")
        void shouldReturnDepartmentById() {
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
            when(departmentMapper.toDto(department)).thenReturn(responseDTO);

            DepartmentResponseDTO result = departmentService.getDepartmentById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
        }

        @Test
        @DisplayName("Given non-existing ID, when getDepartmentById, then throw ResourceNotFoundException")
        void shouldThrowExceptionWhenIdNotFound() {
            when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> departmentService.getDepartmentById(99L));
        }

        @Test
        @DisplayName("When getAllDepartments, then return list of DTOs")
        void shouldReturnAllDepartments() {
            when(departmentRepository.findAll()).thenReturn(List.of(department));
            when(departmentMapper.toDtoList(anyList())).thenReturn(List.of(responseDTO));

            List<DepartmentResponseDTO> result = departmentService.getAllDepartments();

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("Tests de deleteDepartment")
    class DeleteDepartmentTests {

        @Test
        @DisplayName("Given empty department, when deleteDepartment, then delete successfully")
        void shouldDeleteDepartmentSuccessfully() {
            Long deptId = 1L;
            department.setEmployees(new ArrayList<>()); // Pas d'employés
            when(departmentRepository.findById(deptId)).thenReturn(Optional.of(department));

            departmentService.deleteDepartment(deptId);

            verify(departmentRepository, times(1)).delete(department);
        }

        @Test
        @DisplayName("Given department with employees, when deleteDepartment, then throw BusinessException")
        void shouldThrowExceptionWhenDepartmentContainsEmployees() {
            Long deptId = 1L;
            List<Employe> employees = List.of(new Employe());
            department.setEmployees(employees); // Contient 1 employé

            when(departmentRepository.findById(deptId)).thenReturn(Optional.of(department));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> departmentService.deleteDepartment(deptId));
            assertTrue(ex.getMessage().contains("contient encore des employés"));
            verify(departmentRepository, never()).delete(any(Department.class));
        }
    }
}