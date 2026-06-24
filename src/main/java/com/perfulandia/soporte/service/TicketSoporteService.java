package com.perfulandia.soporte.service;

import com.perfulandia.soporte.exception.RecursoNoEncontradoException;
import com.perfulandia.soporte.exception.ReglaNegocioException;
import com.perfulandia.soporte.model.EstadoTicket;
import com.perfulandia.soporte.model.TicketSoporte;
import com.perfulandia.soporte.repository.TicketSoporteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketSoporteService {

    private final TicketSoporteRepository ticketRepository;

    public TicketSoporteService(TicketSoporteRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<TicketSoporte> listarTickets() {
        return ticketRepository.findAll();
    }

    public TicketSoporte buscarPorId(Long idTicket) {
        return ticketRepository.findById(idTicket)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Ticket no encontrado con ID: " + idTicket
                ));
    }

    public List<TicketSoporte> listarPorUsuario(Long idUsuario) {
        return ticketRepository.findByIdUsuario(idUsuario);
    }

    public List<TicketSoporte> listarPorUsuarioAsignado(Long idUsuarioAsignado) {
        return ticketRepository.findByIdUsuarioAsignado(idUsuarioAsignado);
    }

    public TicketSoporte crearTicket(TicketSoporte ticket) {
        ticket.setEstado(EstadoTicket.ABIERTO);
        ticket.setFechaCreacion(LocalDateTime.now());
        ticket.setFechaActualizacion(LocalDateTime.now());
        ticket.setFechaCierre(null);

        return ticketRepository.save(ticket);
    }

    public TicketSoporte actualizarTicket(Long idTicket, TicketSoporte datosActualizados) {
        TicketSoporte ticketExistente = buscarPorId(idTicket);

        if (ticketExistente.getEstado() == EstadoTicket.CERRADO) {
            throw new ReglaNegocioException("No se puede actualizar un ticket cerrado");
        }

        if (ticketExistente.getEstado() == EstadoTicket.CANCELADO) {
            throw new ReglaNegocioException("No se puede actualizar un ticket cancelado");
        }

        ticketExistente.setAsunto(datosActualizados.getAsunto());
        ticketExistente.setDescripcion(datosActualizados.getDescripcion());
        ticketExistente.setPrioridad(datosActualizados.getPrioridad());
        ticketExistente.setFechaActualizacion(LocalDateTime.now());

        return ticketRepository.save(ticketExistente);
    }

    public TicketSoporte cambiarEstado(Long idTicket, EstadoTicket nuevoEstado) {
        TicketSoporte ticket = buscarPorId(idTicket);

        if (ticket.getEstado() == EstadoTicket.CERRADO) {
            throw new ReglaNegocioException("No se puede cambiar el estado de un ticket cerrado");
        }

        if (ticket.getEstado() == EstadoTicket.CANCELADO) {
            throw new ReglaNegocioException("No se puede cambiar el estado de un ticket cancelado");
        }

        ticket.setEstado(nuevoEstado);
        ticket.setFechaActualizacion(LocalDateTime.now());

        if (nuevoEstado == EstadoTicket.CERRADO || nuevoEstado == EstadoTicket.CANCELADO) {
            ticket.setFechaCierre(LocalDateTime.now());
        }

        return ticketRepository.save(ticket);
    }

    public TicketSoporte asignarResponsable(Long idTicket, Long idUsuarioAsignado) {
        TicketSoporte ticket = buscarPorId(idTicket);

        if (ticket.getEstado() == EstadoTicket.CERRADO) {
            throw new ReglaNegocioException("No se puede asignar un ticket cerrado");
        }

        if (ticket.getEstado() == EstadoTicket.CANCELADO) {
            throw new ReglaNegocioException("No se puede asignar un ticket cancelado");
        }

        ticket.setIdUsuarioAsignado(idUsuarioAsignado);
        ticket.setEstado(EstadoTicket.EN_REVISION);
        ticket.setFechaActualizacion(LocalDateTime.now());

        return ticketRepository.save(ticket);
    }

    public TicketSoporte cerrarTicket(Long idTicket) {
        return cambiarEstado(idTicket, EstadoTicket.CERRADO);
    }

    public TicketSoporte cancelarTicket(Long idTicket) {
        return cambiarEstado(idTicket, EstadoTicket.CANCELADO);
    }

    public void eliminarTicket(Long idTicket) {
        TicketSoporte ticket = buscarPorId(idTicket);
        ticketRepository.delete(ticket);
    }
}