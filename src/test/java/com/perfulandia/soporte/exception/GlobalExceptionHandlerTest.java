package com.perfulandia.soporte.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

class GlobalExceptionHandlerTest {

    @Test
    void manejarRecursoNoEncontrado_deberiaRetornar404() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getRequestURI()).thenReturn("/api/tickets/99");

        ResponseEntity<ErrorResponse> response = handler.manejarRecursoNoEncontrado(
                new RecursoNoEncontradoException("Ticket no encontrado con ID: 99"),
                request
        );

        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Recurso no encontrado", response.getBody().getError());
        assertEquals("Ticket no encontrado con ID: 99", response.getBody().getMensaje());
        assertEquals("/api/tickets/99", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void manejarReglaNegocio_deberiaRetornar400() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getRequestURI()).thenReturn("/api/tickets/1");

        ResponseEntity<ErrorResponse> response = handler.manejarReglaNegocio(
                new ReglaNegocioException("No se puede actualizar un ticket cerrado"),
                request
        );

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Regla de negocio incumplida", response.getBody().getError());
        assertEquals("No se puede actualizar un ticket cerrado", response.getBody().getMensaje());
        assertEquals("/api/tickets/1", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void manejarErrorGeneral_deberiaRetornar500() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getRequestURI()).thenReturn("/api/error");

        ResponseEntity<ErrorResponse> response = handler.manejarErrorGeneral(
                new RuntimeException("Error inesperado"),
                request
        );

        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Error interno del servidor", response.getBody().getError());
        assertEquals("Error inesperado", response.getBody().getMensaje());
        assertEquals("/api/error", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }


    @Test
void manejarValidaciones_deberiaRetornar400ConMensajeDeCamposInvalidos() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError errorIdUsuario = new FieldError(
                "ticketSoporte",
                "idUsuario",
                "El idUsuario es obligatorio"
        );

        FieldError errorAsunto = new FieldError(
                "ticketSoporte",
                "asunto",
                "El asunto es obligatorio"
        );

        when(request.getRequestURI()).thenReturn("/api/tickets");
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(errorIdUsuario, errorAsunto));

        ResponseEntity<ErrorResponse> response = handler.manejarValidaciones(
                exception,
                request
        );

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Error de validación", response.getBody().getError());
        assertEquals("/api/tickets", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());

        assertTrue(response.getBody().getMensaje().contains("idUsuario: El idUsuario es obligatorio"));
        assertTrue(response.getBody().getMensaje().contains("asunto: El asunto es obligatorio"));
    }
}