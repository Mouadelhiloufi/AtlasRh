package com.atlasrh.mapper;

import com.atlasrh.dto.employe.EmployeRequestDTO;
import com.atlasrh.dto.employe.EmployeResponseDTO;
import com.atlasrh.entity.Employe;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmployeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "contracts", ignore = true)
    @Mapping(target = "leaveRequests", ignore = true)
    Employe toEntity(EmployeRequestDTO dto);

    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "managerId", source = "manager.id")
    @Mapping(target = "managerFullName", expression = "java(employee.getManager() != null ? employee.getManager().getFirstName() + ' ' + employee.getManager().getLastName() : null)")
    EmployeResponseDTO toDto(Employe employee);

    List<EmployeResponseDTO> toDtoList(List<Employe> employees);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "matricule", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "contracts", ignore = true)
    @Mapping(target = "leaveRequests", ignore = true)
    void updateEntityFromDto(EmployeRequestDTO dto, @MappingTarget Employe entity);
}