package com.perfulandia.soporte.controller;

import com.perfulandia.soporte.dto.AsignarTicketRequest;
import com.perfulandia.soporte.dto.CambiarEstadoTicketRequest;
import com.perfulandia.soporte.model.TicketSoporte;
import com.perfulandia.soporte.service.TicketSoporteService;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketSoporteController {

    private final TicketSoporteService ticketService;

    public TicketSoporteController(TicketSoporteService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public CollectionModel<EntityModel<TicketSoporte>> listarTickets() {
        List<EntityModel<TicketSoporte>> tickets = ticketService.listarTickets()
                .stream()
                .map(this::agregarLinksTicket)
                .toList();

        return CollectionModel.of(
                tickets,
                linkTo(methodOn(TicketSoporteController.class).listarTickets()).withSelfRel()
        );
    }

    @GetMapping("/{idTicket}")
    public EntityModel<TicketSoporte> buscarPorId(@PathVariable Long idTicket) {
        TicketSoporte ticket = ticketService.buscarPorId(idTicket);
        return agregarLinksTicket(ticket);
    }

    @GetMapping("/usuario/{idUsuario}")
    public CollectionModel<EntityModel<TicketSoporte>> listarPorUsuario(@PathVariable Long idUsuario) {
        List<EntityModel<TicketSoporte>> tickets = ticketService.listarPorUsuario(idUsuario)
                .stream()
                .map(this::agregarLinksTicket)
                .toList();

        return CollectionModel.of(
                tickets,
                linkTo(methodOn(TicketSoporteController.class).listarPorUsuario(idUsuario)).withSelfRel(),
                linkTo(methodOn(TicketSoporteController.class).listarTickets()).withRel("todos-los-tickets")
        );
    }

    @GetMapping("/asignado/{idUsuarioAsignado}")
    public CollectionModel<EntityModel<TicketSoporte>> listarPorUsuarioAsignado(
            @PathVariable Long idUsuarioAsignado
    ) {
        List<EntityModel<TicketSoporte>> tickets = ticketService.listarPorUsuarioAsignado(idUsuarioAsignado)
                .stream()
                .map(this::agregarLinksTicket)
                .toList();

        return CollectionModel.of(
                tickets,
                linkTo(methodOn(TicketSoporteController.class)
                        .listarPorUsuarioAsignado(idUsuarioAsignado)).withSelfRel(),
                linkTo(methodOn(TicketSoporteController.class).listarTickets()).withRel("todos-los-tickets")
        );
    }

    @PostMapping
    public ResponseEntity<EntityModel<TicketSoporte>> crearTicket(
            @Valid @RequestBody TicketSoporte ticket
    ) {
        TicketSoporte nuevoTicket = ticketService.crearTicket(ticket);
        return ResponseEntity.ok(agregarLinksTicket(nuevoTicket));
    }

    @PutMapping("/{idTicket}")
    public EntityModel<TicketSoporte> actualizarTicket(
            @PathVariable Long idTicket,
            @Valid @RequestBody TicketSoporte ticket
    ) {
        TicketSoporte ticketActualizado = ticketService.actualizarTicket(idTicket, ticket);
        return agregarLinksTicket(ticketActualizado);
    }

    @PatchMapping("/{idTicket}/estado")
    public EntityModel<TicketSoporte> cambiarEstado(
            @PathVariable Long idTicket,
            @Valid @RequestBody CambiarEstadoTicketRequest request
    ) {
        TicketSoporte ticketActualizado = ticketService.cambiarEstado(idTicket, request.getEstado());
        return agregarLinksTicket(ticketActualizado);
    }

    @PatchMapping("/{idTicket}/asignar")
    public EntityModel<TicketSoporte> asignarResponsable(
            @PathVariable Long idTicket,
            @Valid @RequestBody AsignarTicketRequest request
    ) {
        TicketSoporte ticketAsignado = ticketService.asignarResponsable(
                idTicket,
                request.getIdUsuarioAsignado()
        );

        return agregarLinksTicket(ticketAsignado);
    }

    @PatchMapping("/{idTicket}/cerrar")
    public EntityModel<TicketSoporte> cerrarTicket(@PathVariable Long idTicket) {
        TicketSoporte ticketCerrado = ticketService.cerrarTicket(idTicket);
        return agregarLinksTicket(ticketCerrado);
    }

    @PatchMapping("/{idTicket}/cancelar")
    public EntityModel<TicketSoporte> cancelarTicket(@PathVariable Long idTicket) {
        TicketSoporte ticketCancelado = ticketService.cancelarTicket(idTicket);
        return agregarLinksTicket(ticketCancelado);
    }

    @DeleteMapping("/{idTicket}")
    public ResponseEntity<Void> eliminarTicket(@PathVariable Long idTicket) {
        ticketService.eliminarTicket(idTicket);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<TicketSoporte> agregarLinksTicket(TicketSoporte ticket) {
        return EntityModel.of(
                ticket,
                linkTo(methodOn(TicketSoporteController.class)
                        .buscarPorId(ticket.getIdTicket())).withSelfRel(),
                linkTo(methodOn(TicketSoporteController.class)
                        .listarTickets()).withRel("todos-los-tickets"),
                linkTo(methodOn(TicketSoporteController.class)
                        .listarPorUsuario(ticket.getIdUsuario())).withRel("tickets-del-usuario"),
                linkTo(methodOn(MensajeTicketController.class)
                        .listarMensajes(ticket.getIdTicket())).withRel("mensajes-del-ticket")
        );
    }
}