package com.perfulandia.soporte.exception;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void recursoNoEncontradoException_deberiaGuardarMensaje() {
        RecursoNoEncontradoException exception =
                new RecursoNoEncontradoException("Ticket no encontrado");

        assertEquals("Ticket no encontrado", exception.getMessage());
    }

    @Test
    void reglaNegocioException_deberiaGuardarMensaje() {
        ReglaNegocioException exception =
                new ReglaNegocioException("No se puede actualizar un ticket cerrado");

        assertEquals("No se puede actualizar un ticket cerrado", exception.getMessage());
    }

    @Test
    void errorResponse_deberiaGuardarDatosCorrectamente() {
        LocalDateTime fecha = LocalDateTime.now();

        ErrorResponse error = new ErrorResponse(
                fecha,
                404,
                "Recurso no encontrado",
                "Ticket no encontrado con ID: 99",
                "/api/tickets/99"
        );

        assertEquals(fecha, error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals("Recurso no encontrado", error.getError());
        assertEquals("Ticket no encontrado con ID: 99", error.getMensaje());
        assertEquals("/api/tickets/99", error.getPath());

        error.setStatus(400);
        error.setError("Error de validación");
        error.setMensaje("Campo obligatorio");
        error.setPath("/api/tickets");

        assertEquals(400, error.getStatus());
        assertEquals("Error de validación", error.getError());
        assertEquals("Campo obligatorio", error.getMensaje());
        assertEquals("/api/tickets", error.getPath());
    }
}