package com.atlasrh.dto.leaverequest;

import com.atlasrh.entity.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDecisionDTO {

    @NotNull(message = "L'ID du manager est obligatoire")
    private Long managerId;

    @NotNull(message = "Le statut est obligatoire (APPROUVEE ou REFUSEE)")
    private RequestStatus status;

    @Size(max = 255, message = "Le commentaire ne doit pas dépasser 255 caractères")
    private String comment;
}