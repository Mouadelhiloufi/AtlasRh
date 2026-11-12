package com.atlasrh.service;


import com.atlasrh.dto.employe.EmployeRequestDTO;
import com.atlasrh.dto.employe.EmployeResponseDTO;
import com.atlasrh.entity.Department;
import com.atlasrh.entity.Employe;
import com.atlasrh.exception.BusinessException;
import com.atlasrh.exception.ResourceNotFoundException;
import com.atlasrh.mapper.EmployeMapper;
import com.atlasrh.repository.DepartmentRepository;
import com.atlasrh.repository.EmployeRepository;
import com.atlasrh.service.impl.EmployeServiceImpl;
import jakarta.validation.constraints.AssertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class EmployeServiceImplTest {

    @InjectMocks
    EmployeServiceImpl employeService;

    @Mock
    EmployeRepository employeRepository;

    @Mock
    DepartmentRepository departmentRepository;

    @Mock
    EmployeMapper employeMapper;


    private Employe employe;
    private EmployeRequestDTO requestDTO;
    private EmployeResponseDTO responseDTO;
    private Department department;


    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(10L);

        employe = new Employe();
        employe.setMatricule("EMP001");
        employe.setEmail("mouad@atlasrh.ma");

        requestDTO = new EmployeRequestDTO();
        requestDTO.setMatricule("EMP001");
        requestDTO.setEmail("mouad@atlasrh.ma");
        requestDTO.setDepartmentId(10L);
        requestDTO.setManagerId(null);

        responseDTO = new EmployeResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setMatricule("EMP001");
        responseDTO.setEmail("mouad@atlasrh.ma");
    }

        @Nested
                @DisplayName("Tests de create employee")
        class CreateEmployeeTests{

            @Test
                    @DisplayName("valid dto quand create employe return employeResponseDto")
                    void CreateEmployeSuccessfully(){
                when(employeRepository.existsByMatricule("EMP001")).thenReturn(false);
                when(employeRepository.existsByEmailIgnoreCase("mouad@atlasrh.ma")).thenReturn(false);
                when(employeMapper.toEntity(requestDTO)).thenReturn(employe);
                when(departmentRepository.findById(10L)).thenReturn(Optional.of(department));
                when(employeRepository.save(any(Employe.class))).thenReturn(employe);
                when(employeMapper.toDto(employe)).thenReturn(responseDTO);

                EmployeResponseDTO result = employeService.createEmploye(requestDTO);

                assertNotNull(result);
                assertEquals("EMP001", result.getMatricule());
                verify(employeRepository, times(1)).save(any(Employe.class));
            }
        }

        @Test
        @DisplayName("Given duplicate matricule, when createEmploye, then throw BusinessException")
        void returnExceptionWhenMtriculeExists(){
        when(employeRepository.existsByMatricule("EMP001")).thenReturn(true);
            BusinessException exception = assertThrows(BusinessException.class,
                    ()->employeService.createEmploye(requestDTO));
            assertTrue(exception.getMessage().contains("matricule 'EMP001' existe"));
            verify(employeRepository,never()).save(any(Employe.class));
        }


        @Test
    @DisplayName("duplicate email quand creer employe then throw exception")

    void throwExceptionWhenEmailDuplicate(){
        when(employeRepository.existsByEmailIgnoreCase("mouad@atlasrh.ma")).thenReturn(true);


        BusinessException exception=assertThrows(BusinessException.class,
                ()-> employeService.createEmploye(requestDTO));
        assertTrue(exception.getMessage().contains("email 'mouad@atlasrh.ma' existe"));
        verify(employeRepository,never()).save(any(Employe.class));
        }

    @Test
    @DisplayName("Given non-existing department ID, when createEmploye, then throw ResourceNotFoundException")
    void shouldThrowResourceNotFoundExceptionWhenDepartmentNotFound() {
        // Given
        when(employeRepository.existsByMatricule("EMP001")).thenReturn(false);
        when(employeRepository.existsByEmailIgnoreCase("mouad@atlasrh.ma")).thenReturn(false);
        when(employeMapper.toEntity(requestDTO)).thenReturn(employe);
        when(departmentRepository.findById(10L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> employeService.createEmploye(requestDTO));
        verify(employeRepository, never()).save(any(Employe.class));
    }


    @Nested
    @DisplayName("Tests de updateEmploye")
    class UpdateEmployeTests {

        @Test
        @DisplayName("Given employee as own manager, when updateEmploye, then throw BusinessException")
        void shouldThrowBusinessExceptionWhenEmployeeIsOwnManager() {
            // Given
            Long empId = 1L;
            requestDTO.setManagerId(1L); // Même ID

            when(employeRepository.findById(empId)).thenReturn(Optional.of(employe));
            when(employeRepository.existsByEmailIgnoreCaseAndIdNot("mouad@atlasrh.ma", empId)).thenReturn(false);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> employeService.updateEmploye(empId, requestDTO));
            assertEquals("Un employé ne peut pas être son propre manager.", exception.getMessage());
            verify(employeRepository, never()).save(any(Employe.class));
        }

        @Test
        @DisplayName("Given duplicate email for another ID, when updateEmploye, then throw BusinessException")
        void shouldThrowBusinessExceptionWhenEmailUsedByAnotherEmployee() {
            // Given
            Long empId = 1L;
            when(employeRepository.findById(empId)).thenReturn(Optional.of(employe));
            when(employeRepository.existsByEmailIgnoreCaseAndIdNot("mouad@atlasrh.ma", empId)).thenReturn(true);

            // When & Then
            assertThrows(BusinessException.class, () -> employeService.updateEmploye(empId, requestDTO));
            verify(employeRepository, never()).save(any(Employe.class));
        }

        @Test
        @DisplayName("Given valid data and manager, when updateEmploye, then return updated EmployeResponseDTO")
        void shouldUpdateEmployeSuccessfully() {
            // Given
            Long empId = 1L;
            Long managerId = 2L;
            requestDTO.setManagerId(managerId);

            Employe manager = new Employe();
            manager.setId(managerId);

            when(employeRepository.findById(empId)).thenReturn(Optional.of(employe));
            when(employeRepository.existsByEmailIgnoreCaseAndIdNot("mouad@atlasrh.ma", empId)).thenReturn(false);
            when(departmentRepository.findById(10L)).thenReturn(Optional.of(department));
            when(employeRepository.findById(managerId)).thenReturn(Optional.of(manager));
            when(employeRepository.save(employe)).thenReturn(employe);
            when(employeMapper.toDto(employe)).thenReturn(responseDTO);

            // When
            EmployeResponseDTO result = employeService.updateEmploye(empId, requestDTO);

            // Then
            assertNotNull(result);
            verify(employeRepository, times(1)).save(employe);
        }

    }


    @Nested
    @DisplayName("Tests de consultation (getEmployeById, getAllEmployes, getManagerTeam)")
    class ReadEmployeTests {

        @Test
        @DisplayName("Given existing ID, when getEmployeById, then return DTO")
        void shouldReturnEmployeWhenFound() {
            // Given
            when(employeRepository.findById(1L)).thenReturn(Optional.of(employe));
            when(employeMapper.toDto(employe)).thenReturn(responseDTO);

            // When
            EmployeResponseDTO result = employeService.getEmployeById(1L);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
        }
        @Test
        @DisplayName("Given non-existing ID, when getEmployeById, then throw ResourceNotFoundException")
        void shouldThrowExceptionWhenGetEmployeByIdNotFound() {
            // Given
            when(employeRepository.findById(99L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> employeService.getEmployeById(99L));
        }

        @Test
        @DisplayName("Given pageable, when getAllEmployes, then return page of DTOs")
        void shouldReturnPageOfEmployes() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Employe> page = new PageImpl<>(List.of(employe));

            when(employeRepository.searchEmployes(null, null, pageable)).thenReturn(page);
            when(employeMapper.toDto(employe)).thenReturn(responseDTO);

            // When
            Page<EmployeResponseDTO> result = employeService.getAllEmployes(null, null, pageable);

            // Then
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("Given non-existing manager ID, when getManagerTeam, then throw ResourceNotFoundException")
        void shouldThrowExceptionWhenManagerNotFoundInTeam() {
            // Given
            when(employeRepository.existsById(99L)).thenReturn(false);

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> employeService.getManagerTeam(99L));
        }


    }


}






