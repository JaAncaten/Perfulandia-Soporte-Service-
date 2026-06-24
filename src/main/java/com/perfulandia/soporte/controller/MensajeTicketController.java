package com.perfulandia.soporte.controller;

import com.perfulandia.soporte.model.MensajeTicket;
import com.perfulandia.soporte.service.MensajeTicketService;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/tickets/{idTicket}/mensajes")
public class MensajeTicketController {

    private final MensajeTicketService mensajeService;

    public MensajeTicketController(MensajeTicketService mensajeService) {
        this.mensajeService = mensajeService;
    }

    @GetMapping
    public CollectionModel<EntityModel<MensajeTicket>> listarMensajes(@PathVariable Long idTicket) {
        List<EntityModel<MensajeTicket>> mensajes = mensajeService.listarMensajesPorTicket(idTicket)
                .stream()
                .map(mensaje -> agregarLinksMensaje(idTicket, mensaje))
                .toList();

        return CollectionModel.of(
                mensajes,
                linkTo(methodOn(MensajeTicketController.class).listarMensajes(idTicket)).withSelfRel(),
                linkTo(methodOn(TicketSoporteController.class).buscarPorId(idTicket)).withRel("ticket")
        );
    }

    @PostMapping
    public ResponseEntity<EntityModel<MensajeTicket>> agregarMensaje(
            @PathVariable Long idTicket,
            @Valid @RequestBody MensajeTicket mensaje
    ) {
        MensajeTicket mensajeGuardado = mensajeService.agregarMensaje(idTicket, mensaje);
        return ResponseEntity.ok(agregarLinksMensaje(idTicket, mensajeGuardado));
    }

    @GetMapping("/{idMensaje}")
    public EntityModel<MensajeTicket> buscarMensajePorId(
            @PathVariable Long idTicket,
            @PathVariable Long idMensaje
    ) {
        MensajeTicket mensaje = mensajeService.buscarMensajePorId(idMensaje);
        return agregarLinksMensaje(idTicket, mensaje);
    }

    @DeleteMapping("/{idMensaje}")
    public ResponseEntity<Void> eliminarMensaje(
            @PathVariable Long idTicket,
            @PathVariable Long idMensaje
    ) {
        mensajeService.eliminarMensaje(idMensaje);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<MensajeTicket> agregarLinksMensaje(Long idTicket, MensajeTicket mensaje) {
        return EntityModel.of(
                mensaje,
                linkTo(methodOn(MensajeTicketController.class)
                        .buscarMensajePorId(idTicket, mensaje.getIdMensaje())).withSelfRel(),
                linkTo(methodOn(MensajeTicketController.class)
                        .listarMensajes(idTicket)).withRel("mensajes-del-ticket"),
                linkTo(methodOn(TicketSoporteController.class)
                        .buscarPorId(idTicket)).withRel("ticket")
        );
    }
}