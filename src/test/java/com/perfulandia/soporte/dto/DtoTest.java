package com.perfulandia.soporte.dto;

import com.perfulandia.soporte.model.EstadoTicket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void asignarTicketRequest_deberiaCrearObjetoConConstructorVacioYSetters() {
        AsignarTicketRequest request = new AsignarTicketRequest();

        request.setIdUsuarioAsignado(50L);

        assertEquals(50L, request.getIdUsuarioAsignado());
    }

    @Test
    void asignarTicketRequest_deberiaCrearObjetoConConstructorCompleto() {
        AsignarTicketRequest request = new AsignarTicketRequest(60L);

        assertEquals(60L, request.getIdUsuarioAsignado());
    }

    @Test
    void cambiarEstadoTicketRequest_deberiaCrearObjetoConConstructorVacioYSetters() {
        CambiarEstadoTicketRequest request = new CambiarEstadoTicketRequest();

        request.setEstado(EstadoTicket.EN_REVISION);

        assertEquals(EstadoTicket.EN_REVISION, request.getEstado());
    }

    @Test
    void cambiarEstadoTicketRequest_deberiaCrearObjetoConConstructorCompleto() {
        CambiarEstadoTicketRequest request = new CambiarEstadoTicketRequest(EstadoTicket.CERRADO);

        assertEquals(EstadoTicket.CERRADO, request.getEstado());
    }
}