package com.atlasrh.controller;


import com.atlasrh.dto.employe.EmployeRequestDTO;
import com.atlasrh.dto.employe.EmployeResponseDTO;
import com.atlasrh.service.EmployeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/emolyes")
@RequiredArgsConstructor

public class EmployeController {
    private final EmployeService employeService;

    @PostMapping
    @Operation(summary = "Créer un nouvel employé")
    ResponseEntity<EmployeResponseDTO> createEmploye(@Valid @RequestBody EmployeRequestDTO dto {
        EmployeResponseDTO response=employeService.createEmploye(dto);
        return new ResponseEntity(response, HttpStatus.CREATED);

    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une fiche employé")
    public ResponseEntity<EmployeResponseDTO> updateEmployee(@PathVariable Long id,
                                                              @Valid @RequestBody EmployeRequestDTO dto) {
        return ResponseEntity.ok(employeService.updateEmploye(id, dto));
    }


    @GetMapping
    public ResponseEntity<EmployeResponseDTO> getEmployeById(@PathVariable Long id){
        return ResponseEntity.ok(employeService.getEmployeById(id));
    }


    @GetMapping
    @Operation(summary = "Lister les employés avec pagination, tri et filtres (nom/département)")
    public ResponseEntity<Page<EmployeResponseDTO>> getAllEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @PageableDefault(size = 10, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(employeService.getAllEmployes(search, departmentId, pageable));
    }

    @GetMapping("/{managerId}/team")
    @Operation(summary = "Consulter l'équipe rattachée à un manager")
    public ResponseEntity<List<EmployeResponseDTO>> getManagerTeam(@PathVariable Long managerId) {
        return ResponseEntity.ok(employeService.getManagerTeam(managerId));
    }
}

}
