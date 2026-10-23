package com.atlasrh.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.atlasrh.dto.department.DepartmentRequestDTO;
import com.atlasrh.dto.department.DepartmentResponseDTO;
import com.atlasrh.entity.Department;

import java.util.List;

@Mapper(componentModel = "spring")

public interface DepartmentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employees", ignore = true)
    Department toEntity(DepartmentRequestDTO dto);

    @Mapping(target = "employeeCount",
            expression = "java(department.getEmployees() != null ? department.getEmployees().size() : 0)")
    DepartmentResponseDTO toDto(Department department);

    List<DepartmentResponseDTO> toDtoList(List<Department> departments);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employees", ignore = true)
    void updateEntityFromDto(DepartmentRequestDTO dto, @MappingTarget Department entity);

}
