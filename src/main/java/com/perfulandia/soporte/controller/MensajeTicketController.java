package com.perfulandia.soporte.controller;

import com.perfulandia.soporte.model.MensajeTicket;
import com.perfulandia.soporte.service.MensajeTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/tickets/{idTicket}/mensajes")
@Tag(
        name = "Mensajes de Ticket",
        description = "Endpoints para administrar mensajes asociados a tickets de soporte."
)
public class MensajeTicketController {

    private final MensajeTicketService mensajeService;

    public MensajeTicketController(MensajeTicketService mensajeService) {
        this.mensajeService = mensajeService;
    }

    @GetMapping
    @Operation(
            summary = "Listar mensajes de un ticket",
            description = """
                    Retorna todos los mensajes asociados a un ticket específico.
                    
                    Este endpoint permite revisar la conversación completa entre el cliente
                    y el equipo de soporte dentro de un ticket.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensajes obtenidos correctamente"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public CollectionModel<EntityModel<MensajeTicket>> listarMensajes(
            @Parameter(
                    description = "Identificador del ticket del cual se desean listar los mensajes",
                    example = "1",
                    required = true
            )
            @PathVariable Long idTicket
    ) {
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
    @Operation(
            summary = "Agregar mensaje a un ticket",
            description = """
                    Registra un nuevo mensaje dentro de un ticket de soporte.
                    
                    El mensaje puede ser enviado por un CLIENTE, SOPORTE o ADMIN,
                    dependiendo del tipo de autor definido en la solicitud.
                    
                    No se pueden agregar mensajes a tickets cerrados o cancelados.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensaje agregado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o ticket cerrado/cancelado"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<EntityModel<MensajeTicket>> agregarMensaje(
            @Parameter(
                    description = "Identificador del ticket al que se agregará el mensaje",
                    example = "1",
                    required = true
            )
            @PathVariable Long idTicket,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del mensaje que será agregado al ticket",
                    required = true
            )
            @Valid @RequestBody MensajeTicket mensaje
    ) {
        MensajeTicket mensajeGuardado = mensajeService.agregarMensaje(idTicket, mensaje);
        return ResponseEntity.ok(agregarLinksMensaje(idTicket, mensajeGuardado));
    }

    @GetMapping("/{idMensaje}")
    @Operation(
            summary = "Buscar mensaje por ID",
            description = """
                    Retorna un mensaje específico asociado a un ticket de soporte.
                    
                    Se utiliza para consultar el detalle de un mensaje individual
                    dentro de la conversación del ticket.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensaje encontrado correctamente"),
            @ApiResponse(responseCode = "404", description = "Mensaje no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public EntityModel<MensajeTicket> buscarMensajePorId(
            @Parameter(
                    description = "Identificador del ticket asociado al mensaje",
                    example = "1",
                    required = true
            )
            @PathVariable Long idTicket,

            @Parameter(
                    description = "Identificador del mensaje a consultar",
                    example = "5",
                    required = true
            )
            @PathVariable Long idMensaje
    ) {
        MensajeTicket mensaje = mensajeService.buscarMensajePorId(idMensaje);
        return agregarLinksMensaje(idTicket, mensaje);
    }

    @DeleteMapping("/{idMensaje}")
    @Operation(
            summary = "Eliminar mensaje",
            description = """
                    Elimina un mensaje específico asociado a un ticket de soporte.
                    
                    Si el mensaje no existe, el sistema responderá con error 404.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Mensaje eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Mensaje no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Void> eliminarMensaje(
            @Parameter(
                    description = "Identificador del ticket asociado al mensaje",
                    example = "1",
                    required = true
            )
            @PathVariable Long idTicket,

            @Parameter(
                    description = "Identificador del mensaje que será eliminado",
                    example = "5",
                    required = true
            )
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