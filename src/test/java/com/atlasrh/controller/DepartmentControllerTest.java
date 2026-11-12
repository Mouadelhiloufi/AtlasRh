package com.atlasrh.controller;


import com.atlasrh.dto.department.DepartmentRequestDTO;
import com.atlasrh.dto.department.DepartmentResponseDTO;
import com.atlasrh.entity.Department;
import com.atlasrh.exception.BusinessException;
import com.atlasrh.service.DepartmentService;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DepartmentController.class)
public class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());


    @MockitoBean
    private DepartmentService departmentService;

    private DepartmentResponseDTO responseDTO;
    private DepartmentRequestDTO requestDto;



    @BeforeEach
    void setUp(){
        requestDto=new DepartmentRequestDTO();
        requestDto.setName("engineering");

        responseDTO=new DepartmentResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setName("engineering");
    }


    @Nested
    @DisplayName("Tests de création (POST /api/departments)")
    class DepartmentCreateTest{

        @Test
        @DisplayName("return 201 when department created")
        void shouldReturn201WhenDepartCreated()throws Exception{
            when(departmentService.createDepartment(any(DepartmentRequestDTO.class))).thenReturn(responseDTO);

            mockMvc.perform(post("/api/departments")
                    .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                    .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("engineering"));

        }

        @Test
        @DisplayName("POST api/department should return 400 bad request when name already exists")
        void shouldReturn400WhenNameAlreadyExists()throws Exception{
            when(departmentService.createDepartment(any(DepartmentRequestDTO.class)))
                    .thenThrow(new BusinessException("email already exists"));

            mockMvc.perform(post("/api/departments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto))
            ).andExpect(status().isBadRequest());

        }

        @Test
        @DisplayName("POST api/departmentss should return bad request when blank name invalid")
        void souldReturnBadRequqestWhenValidationFails()throws Exception{
            DepartmentRequestDTO invalid=new DepartmentRequestDTO();
            invalid.setName("");

            mockMvc.perform(post("/api/departments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalid))
            ).andExpect(status().isBadRequest());
        }

    }


}
