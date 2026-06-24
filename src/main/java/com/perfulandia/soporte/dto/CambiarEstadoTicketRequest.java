package com.perfulandia.soporte.dto;

import com.perfulandia.soporte.model.EstadoTicket;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CambiarEstadoTicketRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoTicket estado;
}