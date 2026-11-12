package com.atlasrh.controller;

import com.atlasrh.dto.employe.EmployeRequestDTO;
import com.atlasrh.dto.employe.EmployeResponseDTO;
import com.atlasrh.exception.BusinessException;
import com.atlasrh.exception.GlobalExceptionHandler;
import com.atlasrh.exception.ResourceNotFoundException;
import com.atlasrh.service.EmployeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeController.class)
@ContextConfiguration(classes = {EmployeController.class, GlobalExceptionHandler.class})
class EmployeControllerTest {

    private final MockMvc mockMvc;
private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    @MockitoBean
    private EmployeService employeService;

    public EmployeControllerTest(@Autowired MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeEach
    void setUp() {
        requestDTO = new EmployeRequestDTO();
        requestDTO.setMatricule("EMP001");
        requestDTO.setEmail("mouad@atlasrh.ma");
        requestDTO.setFirstName("Mouad");
        requestDTO.setLastName("El Hiloufi");
        requestDTO.setHireDate(LocalDate.now().minusYears(1));

        responseDTO = new EmployeResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setMatricule("EMP001");
        responseDTO.setEmail("mouad@atlasrh.ma");
        responseDTO.setFirstName("Mouad");
        responseDTO.setLastName("El Hiloufi");
    }

    private EmployeRequestDTO requestDTO;
    private EmployeResponseDTO responseDTO;

    @Nested
    @DisplayName("Tests de création (POST /api/employes)")
    class CreateEmployeTests {

        @Test
        @DisplayName("POST /api/employes - Should return 201 Created when valid")
        void shouldCreateEmployeAndReturn201() throws Exception {
            when(employeService.createEmploye(any(EmployeRequestDTO.class))).thenReturn(responseDTO);

            mockMvc.perform(post("/api/employes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.matricule").value("EMP001"))
                    .andExpect(jsonPath("$.email").value("mouad@atlasrh.ma"));
        }

        @Test
        @DisplayName("POST /api/employes - Should return 400 Bad Request on duplicate business rule")
        void shouldReturn400WhenBusinessException() throws Exception {
            when(employeService.createEmploye(any(EmployeRequestDTO.class)))
                    .thenThrow(new BusinessException("Matricule existant"));

            mockMvc.perform(post("/api/employes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Tests de mise à jour (PUT /api/employes/{id})")
    class UpdateEmployeTests {

        @Test
        @DisplayName("PUT /api/employes/1 - Should return 200 OK on successful update")
        void shouldUpdateEmployeAndReturn200() throws Exception {
            when(employeService.updateEmploye(eq(1L), any(EmployeRequestDTO.class))).thenReturn(responseDTO);

            mockMvc.perform(put("/api/employes/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("PUT /api/employes/1 - Should return 400 Bad Request when setting own manager")
        void shouldReturn400WhenSelfManager() throws Exception {
            when(employeService.updateEmploye(eq(1L), any(EmployeRequestDTO.class)))
                    .thenThrow(new BusinessException("Un employé ne peut pas être son propre manager."));

            mockMvc.perform(put("/api/employes/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Tests de consultation (GET /api/employes)")
    class GetEmployeTests {

        @Test
        @DisplayName("GET /api/employes/1 - Should return 200 OK when found")
        void shouldReturn200WhenFound() throws Exception {
            when(employeService.getEmployeById(1L)).thenReturn(responseDTO);

            mockMvc.perform(get("/api/employes/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.matricule").value("EMP001"));
        }

        @Test
        @DisplayName("GET /api/employes/99 - Should return 404 Not Found when missing")
        void shouldReturn404WhenNotFound() throws Exception {
            when(employeService.getEmployeById(99L))
                    .thenThrow(new ResourceNotFoundException("Employé non trouvé"));

            mockMvc.perform(get("/api/employes/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /api/employes - Should return paginated list")
        void shouldReturnPaginatedEmployes() throws Exception {
            Page<EmployeResponseDTO> page = new PageImpl<>(List.of(responseDTO));
            when(employeService.getAllEmployes(any(), any(), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/employes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].matricule").value("EMP001"));
        }

        @Test
        @DisplayName("GET /api/employes/1/team - Should return manager team list")
        void shouldReturnManagerTeam() throws Exception {
            when(employeService.getManagerTeam(1L)).thenReturn(List.of(responseDTO));

            mockMvc.perform(get("/api/employes/1/team"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1));
        }
    }
}