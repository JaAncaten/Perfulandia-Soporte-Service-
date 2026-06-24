package com.perfulandia.soporte.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MensajeTicketTest {

    @Test
    void prePersist_deberiaAsignarFechaEnvio() {
        MensajeTicket mensaje = MensajeTicket.builder()
                .idUsuario(10L)
                .mensaje("Necesito ayuda")
                .tipoAutor(TipoAutorMensaje.CLIENTE)
                .build();

        mensaje.prePersist();

        assertNotNull(mensaje.getFechaEnvio());
    }

    @Test
    void gettersSettersBuilder_deberianFuncionarCorrectamente() {
        TicketSoporte ticket = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Consulta")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.MEDIA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        MensajeTicket mensaje = MensajeTicket.builder()
                .idMensaje(1L)
                .ticket(ticket)
                .idUsuario(10L)
                .mensaje("Mensaje de prueba")
                .tipoAutor(TipoAutorMensaje.CLIENTE)
                .build();

        assertEquals(1L, mensaje.getIdMensaje());
        assertEquals(ticket, mensaje.getTicket());
        assertEquals(10L, mensaje.getIdUsuario());
        assertEquals("Mensaje de prueba", mensaje.getMensaje());
        assertEquals(TipoAutorMensaje.CLIENTE, mensaje.getTipoAutor());

        mensaje.setMensaje("Mensaje actualizado");
        mensaje.setTipoAutor(TipoAutorMensaje.SOPORTE);

        assertEquals("Mensaje actualizado", mensaje.getMensaje());
        assertEquals(TipoAutorMensaje.SOPORTE, mensaje.getTipoAutor());
    }

    @Test
    void constructorVacio_deberiaCrearObjetoSinDatos() {
        MensajeTicket mensaje = new MensajeTicket();

        assertNotNull(mensaje);
        assertNull(mensaje.getIdMensaje());
    }
}