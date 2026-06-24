package com.perfulandia.soporte.service;

import com.perfulandia.soporte.exception.RecursoNoEncontradoException;
import com.perfulandia.soporte.exception.ReglaNegocioException;
import com.perfulandia.soporte.model.EstadoTicket;
import com.perfulandia.soporte.model.MensajeTicket;
import com.perfulandia.soporte.model.TicketSoporte;
import com.perfulandia.soporte.repository.MensajeTicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MensajeTicketService {

    private final MensajeTicketRepository mensajeRepository;
    private final TicketSoporteService ticketService;

    public MensajeTicketService(
            MensajeTicketRepository mensajeRepository,
            TicketSoporteService ticketService
    ) {
        this.mensajeRepository = mensajeRepository;
        this.ticketService = ticketService;
    }

    public List<MensajeTicket> listarMensajesPorTicket(Long idTicket) {
        return mensajeRepository.findByTicketIdTicket(idTicket);
    }

    public MensajeTicket agregarMensaje(Long idTicket, MensajeTicket mensaje) {
        TicketSoporte ticket = ticketService.buscarPorId(idTicket);

        boolean ticketCerrado = ticket.getEstado() == EstadoTicket.CERRADO;
        boolean ticketCancelado = ticket.getEstado() == EstadoTicket.CANCELADO;

        if (ticketCerrado || ticketCancelado) {
            String mensajeError = "No se pueden agregar mensajes a un ticket cerrado o cancelado";
            throw new ReglaNegocioException(mensajeError);
        }

        mensaje.setTicket(ticket);
        mensaje.setFechaEnvio(LocalDateTime.now());

        MensajeTicket mensajeGuardado = mensajeRepository.save(mensaje);

        ticket.setEstado(EstadoTicket.RESPONDIDO);
        ticket.setFechaActualizacion(LocalDateTime.now());

        return mensajeGuardado;
    }

    public MensajeTicket buscarMensajePorId(Long idMensaje) {
        return mensajeRepository.findById(idMensaje)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Mensaje no encontrado con ID: " + idMensaje
                ));
    }

    public void eliminarMensaje(Long idMensaje) {
        MensajeTicket mensaje = buscarMensajePorId(idMensaje);
        mensajeRepository.delete(mensaje);
    }
}