package com.perfulandia.soporte.service;

import com.perfulandia.soporte.exception.RecursoNoEncontradoException;
import com.perfulandia.soporte.exception.ReglaNegocioException;
import com.perfulandia.soporte.model.EstadoTicket;
import com.perfulandia.soporte.model.PrioridadTicket;
import com.perfulandia.soporte.model.TicketSoporte;
import com.perfulandia.soporte.repository.TicketSoporteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketSoporteServiceTest {

    @Mock
    private TicketSoporteRepository ticketRepository;

    @InjectMocks
    private TicketSoporteService ticketService;

    @Test
    void listarTickets_deberiaRetornarTodosLosTickets() {
        TicketSoporte ticket = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Consulta")
                .descripcion("Necesito ayuda")
                .prioridad(PrioridadTicket.MEDIA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        when(ticketRepository.findAll()).thenReturn(List.of(ticket));

        List<TicketSoporte> resultado = ticketService.listarTickets();

        assertEquals(1, resultado.size());
        assertEquals("Consulta", resultado.get(0).getAsunto());
        verify(ticketRepository).findAll();
    }

    @Test
    void buscarPorId_deberiaRetornarTicketSiExiste() {
        TicketSoporte ticket = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        TicketSoporte resultado = ticketService.buscarPorId(1L);

        assertEquals(1L, resultado.getIdTicket());
        assertEquals("Problema", resultado.getAsunto());
        verify(ticketRepository).findById(1L);
    }

    @Test
    void buscarPorId_deberiaLanzarExcepcionSiNoExiste() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> ticketService.buscarPorId(99L)
        );

        assertEquals("Ticket no encontrado con ID: 99", exception.getMessage());
        verify(ticketRepository).findById(99L);
    }

    @Test
    void listarPorUsuario_deberiaRetornarTicketsDelUsuario() {
        TicketSoporte ticket = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Consulta")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.MEDIA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        when(ticketRepository.findByIdUsuario(10L)).thenReturn(List.of(ticket));

        List<TicketSoporte> resultado = ticketService.listarPorUsuario(10L);

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getIdUsuario());
        verify(ticketRepository).findByIdUsuario(10L);
    }

    @Test
    void listarPorUsuarioAsignado_deberiaRetornarTicketsAsignados() {
        TicketSoporte ticket = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .idUsuarioAsignado(50L)
                .asunto("Ticket asignado")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.EN_REVISION)
                .build();

        when(ticketRepository.findByIdUsuarioAsignado(50L)).thenReturn(List.of(ticket));

        List<TicketSoporte> resultado = ticketService.listarPorUsuarioAsignado(50L);

        assertEquals(1, resultado.size());
        assertEquals(50L, resultado.get(0).getIdUsuarioAsignado());
        verify(ticketRepository).findByIdUsuarioAsignado(50L);
    }

    @Test
    void crearTicket_deberiaGuardarTicketConEstadoAbierto() {
        TicketSoporte ticket = TicketSoporte.builder()
                .idUsuario(10L)
                .asunto("Nuevo ticket")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .build();

        when(ticketRepository.save(any(TicketSoporte.class))).thenAnswer(invocation -> {
            TicketSoporte ticketGuardado = invocation.getArgument(0);
            ticketGuardado.setIdTicket(1L);
            return ticketGuardado;
        });

        TicketSoporte resultado = ticketService.crearTicket(ticket);

        assertEquals(1L, resultado.getIdTicket());
        assertEquals(EstadoTicket.ABIERTO, resultado.getEstado());
        assertEquals("Nuevo ticket", resultado.getAsunto());
        assertNotNull(resultado.getFechaCreacion());
        assertNotNull(resultado.getFechaActualizacion());
        assertNull(resultado.getFechaCierre());
        verify(ticketRepository).save(ticket);
    }

    @Test
    void actualizarTicket_deberiaActualizarDatosSiEstaAbierto() {
        TicketSoporte existente = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Antiguo")
                .descripcion("Antigua descripción")
                .prioridad(PrioridadTicket.MEDIA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        TicketSoporte datosActualizados = TicketSoporte.builder()
                .asunto("Nuevo")
                .descripcion("Nueva descripción")
                .prioridad(PrioridadTicket.URGENTE)
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(ticketRepository.save(any(TicketSoporte.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketSoporte resultado = ticketService.actualizarTicket(1L, datosActualizados);

        assertEquals("Nuevo", resultado.getAsunto());
        assertEquals("Nueva descripción", resultado.getDescripcion());
        assertEquals(PrioridadTicket.URGENTE, resultado.getPrioridad());
        assertNotNull(resultado.getFechaActualizacion());
        verify(ticketRepository).findById(1L);
        verify(ticketRepository).save(existente);
    }

    @Test
    void actualizarTicket_deberiaLanzarExcepcionSiTicketEstaCerrado() {
        TicketSoporte ticketCerrado = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Ticket cerrado")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.CERRADO)
                .build();

        TicketSoporte datosActualizados = TicketSoporte.builder()
                .asunto("Nuevo asunto")
                .descripcion("Nueva descripción")
                .prioridad(PrioridadTicket.MEDIA)
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticketCerrado));

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> ticketService.actualizarTicket(1L, datosActualizados)
        );

        assertEquals("No se puede actualizar un ticket cerrado", exception.getMessage());
        verify(ticketRepository).findById(1L);
        verify(ticketRepository, never()).save(any(TicketSoporte.class));
    }

    @Test
    void actualizarTicket_deberiaLanzarExcepcionSiTicketEstaCancelado() {
        TicketSoporte ticketCancelado = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Ticket cancelado")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.CANCELADO)
                .build();

        TicketSoporte datosActualizados = TicketSoporte.builder()
                .asunto("Nuevo asunto")
                .descripcion("Nueva descripción")
                .prioridad(PrioridadTicket.MEDIA)
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticketCancelado));

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> ticketService.actualizarTicket(1L, datosActualizados)
        );

        assertEquals("No se puede actualizar un ticket cancelado", exception.getMessage());
        verify(ticketRepository).findById(1L);
        verify(ticketRepository, never()).save(any(TicketSoporte.class));
    }

    @Test
    void cambiarEstado_deberiaCambiarEstadoDelTicket() {
        TicketSoporte ticket = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(TicketSoporte.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketSoporte resultado = ticketService.cambiarEstado(1L, EstadoTicket.EN_REVISION);

        assertEquals(EstadoTicket.EN_REVISION, resultado.getEstado());
        assertNotNull(resultado.getFechaActualizacion());
        verify(ticketRepository).findById(1L);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void cambiarEstado_deberiaAsignarFechaCierreSiNuevoEstadoEsCerrado() {
        TicketSoporte ticket = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(TicketSoporte.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketSoporte resultado = ticketService.cambiarEstado(1L, EstadoTicket.CERRADO);

        assertEquals(EstadoTicket.CERRADO, resultado.getEstado());
        assertNotNull(resultado.getFechaCierre());
        verify(ticketRepository).findById(1L);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void cambiarEstado_deberiaAsignarFechaCierreSiNuevoEstadoEsCancelado() {
        TicketSoporte ticket = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(TicketSoporte.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketSoporte resultado = ticketService.cambiarEstado(1L, EstadoTicket.CANCELADO);

        assertEquals(EstadoTicket.CANCELADO, resultado.getEstado());
        assertNotNull(resultado.getFechaCierre());
        verify(ticketRepository).findById(1L);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void cambiarEstado_deberiaLanzarExcepcionSiTicketEstaCerrado() {
        TicketSoporte ticketCerrado = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Ticket cerrado")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.CERRADO)
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticketCerrado));

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> ticketService.cambiarEstado(1L, EstadoTicket.EN_REVISION)
        );

        assertEquals("No se puede cambiar el estado de un ticket cerrado", exception.getMessage());
        verify(ticketRepository).findById(1L);
        verify(ticketRepository, never()).save(any(TicketSoporte.class));
    }

    @Test
    void cambiarEstado_deberiaLanzarExcepcionSiTicketEstaCancelado() {
        TicketSoporte ticketCancelado = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Ticket cancelado")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.CANCELADO)
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticketCancelado));

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> ticketService.cambiarEstado(1L, EstadoTicket.EN_REVISION)
        );

        assertEquals("No se puede cambiar el estado de un ticket cancelado", exception.getMessage());
        verify(ticketRepository).findById(1L);
        verify(ticketRepository, never()).save(any(TicketSoporte.class));
    }

    @Test
    void asignarResponsable_deberiaAsignarUsuarioYCambiarEstado() {
        TicketSoporte ticket = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(TicketSoporte.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketSoporte resultado = ticketService.asignarResponsable(1L, 50L);

        assertEquals(50L, resultado.getIdUsuarioAsignado());
        assertEquals(EstadoTicket.EN_REVISION, resultado.getEstado());
        assertNotNull(resultado.getFechaActualizacion());
        verify(ticketRepository).findById(1L);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void asignarResponsable_deberiaLanzarExcepcionSiTicketEstaCerrado() {
        TicketSoporte ticketCerrado = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Ticket cerrado")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.CERRADO)
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticketCerrado));

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> ticketService.asignarResponsable(1L, 50L)
        );

        assertEquals("No se puede asignar un ticket cerrado", exception.getMessage());
        verify(ticketRepository).findById(1L);
        verify(ticketRepository, never()).save(any(TicketSoporte.class));
    }

    @Test
    void asignarResponsable_deberiaLanzarExcepcionSiTicketEstaCancelado() {
        TicketSoporte ticketCancelado = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Ticket cancelado")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.CANCELADO)
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticketCancelado));

        ReglaNegocioException exception = assertThrows(
                ReglaNegocioException.class,
                () -> ticketService.asignarResponsable(1L, 50L)
        );

        assertEquals("No se puede asignar un ticket cancelado", exception.getMessage());
        verify(ticketRepository).findById(1L);
        verify(ticketRepository, never()).save(any(TicketSoporte.class));
    }

    @Test
    void cerrarTicket_deberiaCerrarTicket() {
        TicketSoporte ticket = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(TicketSoporte.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketSoporte resultado = ticketService.cerrarTicket(1L);

        assertEquals(EstadoTicket.CERRADO, resultado.getEstado());
        assertNotNull(resultado.getFechaCierre());
        verify(ticketRepository).findById(1L);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void cancelarTicket_deberiaCancelarTicket() {
        TicketSoporte ticket = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(TicketSoporte.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketSoporte resultado = ticketService.cancelarTicket(1L);

        assertEquals(EstadoTicket.CANCELADO, resultado.getEstado());
        assertNotNull(resultado.getFechaCierre());
        verify(ticketRepository).findById(1L);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void eliminarTicket_deberiaEliminarTicketExistente() {
        TicketSoporte ticket = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        doNothing().when(ticketRepository).delete(ticket);

        ticketService.eliminarTicket(1L);

        verify(ticketRepository).findById(1L);
        verify(ticketRepository).delete(ticket);
    }
}