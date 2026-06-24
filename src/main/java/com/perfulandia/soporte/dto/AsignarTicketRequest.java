package com.perfulandia.soporte.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsignarTicketRequest {

    @NotNull(message = "El idUsuarioAsignado es obligatorio")
    private Long idUsuarioAsignado;
}