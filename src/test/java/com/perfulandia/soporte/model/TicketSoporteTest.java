package com.perfulandia.soporte.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class TicketSoporteTest {

    @Test
    void prePersist_deberiaAsignarFechasEstadoYPrioridadPorDefecto() {
        TicketSoporte ticket = TicketSoporte.builder()
                .idUsuario(10L)
                .asunto("Consulta")
                .descripcion("Necesito ayuda")
                .build();

        ticket.prePersist();

        assertNotNull(ticket.getFechaCreacion());
        assertNotNull(ticket.getFechaActualizacion());
        assertEquals(EstadoTicket.ABIERTO, ticket.getEstado());
        assertEquals(PrioridadTicket.MEDIA, ticket.getPrioridad());
    }

    @Test
    void prePersist_noDeberiaCambiarEstadoYPrioridadSiYaExisten() {
        TicketSoporte ticket = TicketSoporte.builder()
                .idUsuario(10L)
                .asunto("Consulta")
                .descripcion("Necesito ayuda")
                .estado(EstadoTicket.EN_REVISION)
                .prioridad(PrioridadTicket.URGENTE)
                .build();

        ticket.prePersist();

        assertEquals(EstadoTicket.EN_REVISION, ticket.getEstado());
        assertEquals(PrioridadTicket.URGENTE, ticket.getPrioridad());
        assertNotNull(ticket.getFechaCreacion());
        assertNotNull(ticket.getFechaActualizacion());
    }

    @Test
    void preUpdate_deberiaActualizarFechaActualizacion() {
        TicketSoporte ticket = TicketSoporte.builder()
                .idUsuario(10L)
                .asunto("Consulta")
                .descripcion("Necesito ayuda")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        ticket.preUpdate();

        assertNotNull(ticket.getFechaActualizacion());
    }

    @Test
    void gettersSettersBuilder_deberianFuncionarCorrectamente() {
        TicketSoporte ticket = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Problema con pedido")
                .descripcion("Mi pedido no llegó")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .idUsuarioAsignado(50L)
                .mensajes(new ArrayList<>())
                .build();

        assertEquals(1L, ticket.getIdTicket());
        assertEquals(10L, ticket.getIdUsuario());
        assertEquals("Problema con pedido", ticket.getAsunto());
        assertEquals("Mi pedido no llegó", ticket.getDescripcion());
        assertEquals(PrioridadTicket.ALTA, ticket.getPrioridad());
        assertEquals(EstadoTicket.ABIERTO, ticket.getEstado());
        assertEquals(50L, ticket.getIdUsuarioAsignado());
        assertNotNull(ticket.getMensajes());

        ticket.setAsunto("Asunto actualizado");
        ticket.setDescripcion("Descripción actualizada");

        assertEquals("Asunto actualizado", ticket.getAsunto());
        assertEquals("Descripción actualizada", ticket.getDescripcion());
    }

    @Test
    void constructorVacio_deberiaCrearObjetoSinDatos() {
        TicketSoporte ticket = new TicketSoporte();

        assertNotNull(ticket);
        assertNull(ticket.getIdTicket());
    }
}