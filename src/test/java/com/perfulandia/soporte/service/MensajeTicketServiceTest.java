package com.perfulandia.soporte.service;

import com.perfulandia.soporte.exception.ReglaNegocioException;
import com.perfulandia.soporte.model.EstadoTicket;
import com.perfulandia.soporte.model.MensajeTicket;
import com.perfulandia.soporte.model.PrioridadTicket;
import com.perfulandia.soporte.model.TicketSoporte;
import com.perfulandia.soporte.model.TipoAutorMensaje;
import com.perfulandia.soporte.repository.MensajeTicketRepository;
import com.perfulandia.soporte.repository.TicketSoporteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MensajeTicketServiceTest {

    @Mock
    private MensajeTicketRepository mensajeRepository;

    @Mock
    private TicketSoporteRepository ticketRepository;

    @InjectMocks
    private MensajeTicketService mensajeTicketService;

    @Test
    void agregarMensaje_cuandoTicketEstaCerrado_deberiaLanzarReglaNegocioException() {
        Long idTicket = 1L;

        TicketSoporte ticketCerrado = TicketSoporte.builder()
                .idTicket(idTicket)
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripcion")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.CERRADO)
                .build();

        MensajeTicket mensaje = MensajeTicket.builder()
                .idUsuario(10L)
                .mensaje("Necesito ayuda")
                .tipoAutor(TipoAutorMensaje.CLIENTE)
                .build();

        when(ticketRepository.findById(idTicket)).thenReturn(Optional.of(ticketCerrado));

        assertThrows(ReglaNegocioException.class, () ->
                mensajeTicketService.agregarMensaje(idTicket, mensaje)
        );

        verify(ticketRepository).findById(idTicket);
        verify(mensajeRepository, never()).save(any(MensajeTicket.class));
    }

    @Test
    void agregarMensaje_cuandoTicketEstaCancelado_deberiaLanzarReglaNegocioException() {
        Long idTicket = 1L;

        TicketSoporte ticketCancelado = TicketSoporte.builder()
                .idTicket(idTicket)
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripcion")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.CANCELADO)
                .build();

        MensajeTicket mensaje = MensajeTicket.builder()
                .idUsuario(10L)
                .mensaje("Necesito ayuda")
                .tipoAutor(TipoAutorMensaje.CLIENTE)
                .build();

        when(ticketRepository.findById(idTicket)).thenReturn(Optional.of(ticketCancelado));

        assertThrows(ReglaNegocioException.class, () ->
                mensajeTicketService.agregarMensaje(idTicket, mensaje)
        );

        verify(ticketRepository).findById(idTicket);
        verify(mensajeRepository, never()).save(any(MensajeTicket.class));
    }
}