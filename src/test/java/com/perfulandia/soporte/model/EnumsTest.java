package com.perfulandia.soporte.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumTest {

    @Test
    void estadoTicket_deberiaContenerTodosLosValores() {
        assertEquals(EstadoTicket.ABIERTO, EstadoTicket.valueOf("ABIERTO"));
        assertEquals(EstadoTicket.EN_REVISION, EstadoTicket.valueOf("EN_REVISION"));
        assertEquals(EstadoTicket.RESPONDIDO, EstadoTicket.valueOf("RESPONDIDO"));
        assertEquals(EstadoTicket.CERRADO, EstadoTicket.valueOf("CERRADO"));
        assertEquals(EstadoTicket.CANCELADO, EstadoTicket.valueOf("CANCELADO"));
        assertEquals(5, EstadoTicket.values().length);
    }

    @Test
    void prioridadTicket_deberiaContenerTodosLosValores() {
        assertEquals(PrioridadTicket.BAJA, PrioridadTicket.valueOf("BAJA"));
        assertEquals(PrioridadTicket.MEDIA, PrioridadTicket.valueOf("MEDIA"));
        assertEquals(PrioridadTicket.ALTA, PrioridadTicket.valueOf("ALTA"));
        assertEquals(PrioridadTicket.URGENTE, PrioridadTicket.valueOf("URGENTE"));
        assertEquals(4, PrioridadTicket.values().length);
    }

    @Test
    void tipoAutorMensaje_deberiaContenerTodosLosValores() {
        assertEquals(TipoAutorMensaje.CLIENTE, TipoAutorMensaje.valueOf("CLIENTE"));
        assertEquals(TipoAutorMensaje.SOPORTE, TipoAutorMensaje.valueOf("SOPORTE"));
        assertEquals(TipoAutorMensaje.ADMIN, TipoAutorMensaje.valueOf("ADMIN"));
        assertEquals(3, TipoAutorMensaje.values().length);
    }
}