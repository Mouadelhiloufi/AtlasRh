package com.atlasrh.mapper;

import com.atlasrh.dto.leaverequest.LeaveRequestCreateDTO;
import com.atlasrh.dto.leaverequest.LeaveRequestResponseDTO;
import com.atlasrh.entity.LeaveRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LeaveRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employe", ignore = true)
    @Mapping(target = "leaveType", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "comment", ignore = true)
    LeaveRequest toEntity(LeaveRequestCreateDTO dto);

    @Mapping(target = "employeeId", source = "employe.id")
    @Mapping(target = "employeeFullName", expression = "java(leaveRequest.getEmploye() != null ? leaveRequest.getEmploye().getFirstName() + ' ' + leaveRequest.getEmploye().getLastName() : null)")
    @Mapping(target = "leaveTypeId", source = "leaveType.id")
    @Mapping(target = "leaveTypeName", source = "leaveType.name")
    @Mapping(target = "workingDaysCount", ignore = true) // Calculé dynamiquement dans le service
    LeaveRequestResponseDTO toDto(LeaveRequest leaveRequest);

    List<LeaveRequestResponseDTO> toDtoList(List<LeaveRequest> leaveRequests);
}