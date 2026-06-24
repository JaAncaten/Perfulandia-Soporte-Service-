package com.perfulandia.soporte.controller;

import com.perfulandia.soporte.dto.AsignarTicketRequest;
import com.perfulandia.soporte.dto.CambiarEstadoTicketRequest;
import com.perfulandia.soporte.model.TicketSoporte;
import com.perfulandia.soporte.service.TicketSoporteService;
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
@RequestMapping("/api/tickets")
@Tag(
        name = "Tickets de Soporte",
        description = "Endpoints para administrar tickets de soporte técnico de Perfulandia."
)
public class TicketSoporteController {

    private final TicketSoporteService ticketService;

    public TicketSoporteController(TicketSoporteService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    @Operation(
            summary = "Listar todos los tickets",
            description = """
                    Retorna todos los tickets registrados en el microservicio de soporte.

                    Este endpoint permite al equipo administrativo o de soporte
                    consultar la totalidad de tickets existentes en el sistema.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tickets obtenidos correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
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
    @Operation(
            summary = "Buscar ticket por ID",
            description = """
                    Retorna el detalle de un ticket específico según su identificador.

                    La respuesta incluye enlaces HATEOAS para navegar al listado general,
                    tickets del usuario y mensajes asociados al ticket.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket encontrado correctamente"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public EntityModel<TicketSoporte> buscarPorId(
            @Parameter(
                    description = "Identificador único del ticket",
                    example = "1",
                    required = true
            )
            @PathVariable Long idTicket
    ) {
        TicketSoporte ticket = ticketService.buscarPorId(idTicket);
        return agregarLinksTicket(ticket);
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(
            summary = "Listar tickets por usuario",
            description = """
                    Retorna todos los tickets creados por un usuario específico.

                    Este endpoint permite consultar el historial de solicitudes
                    de soporte realizadas por un cliente.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tickets del usuario obtenidos correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public CollectionModel<EntityModel<TicketSoporte>> listarPorUsuario(
            @Parameter(
                    description = "Identificador del usuario que creó los tickets",
                    example = "10",
                    required = true
            )
            @PathVariable Long idUsuario
    ) {
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
    @Operation(
            summary = "Listar tickets asignados a un responsable",
            description = """
                    Retorna todos los tickets asignados a un usuario responsable.

                    Se utiliza para que el personal de soporte pueda revisar
                    los tickets que tiene asignados.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tickets asignados obtenidos correctamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public CollectionModel<EntityModel<TicketSoporte>> listarPorUsuarioAsignado(
            @Parameter(
                    description = "Identificador del usuario responsable asignado",
                    example = "50",
                    required = true
            )
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
    @Operation(
            summary = "Crear un nuevo ticket de soporte",
            description = """
                    Permite crear un nuevo ticket de soporte.

                    Al crear un ticket:
                    - El estado inicial queda como ABIERTO.
                    - Se registra la fecha de creación.
                    - Si no se define prioridad, se asigna prioridad MEDIA.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<EntityModel<TicketSoporte>> crearTicket(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del ticket que será creado",
                    required = true
            )
            @Valid @RequestBody TicketSoporte ticket
    ) {
        TicketSoporte nuevoTicket = ticketService.crearTicket(ticket);
        return ResponseEntity.ok(agregarLinksTicket(nuevoTicket));
    }

    @PutMapping("/{idTicket}")
    @Operation(
            summary = "Actualizar un ticket existente",
            description = """
                    Actualiza asunto, descripción y prioridad de un ticket existente.

                    No se permite actualizar tickets que estén en estado CERRADO
                    o CANCELADO.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "No se puede actualizar el ticket o los datos son inválidos"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public EntityModel<TicketSoporte> actualizarTicket(
            @Parameter(
                    description = "Identificador del ticket que será actualizado",
                    example = "1",
                    required = true
            )
            @PathVariable Long idTicket,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos actualizados del ticket",
                    required = true
            )
            @Valid @RequestBody TicketSoporte ticket
    ) {
        TicketSoporte ticketActualizado = ticketService.actualizarTicket(idTicket, ticket);
        return agregarLinksTicket(ticketActualizado);
    }

    @PatchMapping("/{idTicket}/estado")
    @Operation(
            summary = "Cambiar estado de un ticket",
            description = """
                    Permite modificar el estado actual de un ticket.

                    Estados disponibles:
                    - ABIERTO
                    - EN_REVISION
                    - RESPONDIDO
                    - CERRADO
                    - CANCELADO

                    Si el nuevo estado es CERRADO o CANCELADO,
                    se registra la fecha de cierre.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado inválido o regla de negocio incumplida"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public EntityModel<TicketSoporte> cambiarEstado(
            @Parameter(
                    description = "Identificador del ticket al que se cambiará el estado",
                    example = "1",
                    required = true
            )
            @PathVariable Long idTicket,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevo estado del ticket",
                    required = true
            )
            @Valid @RequestBody CambiarEstadoTicketRequest request
    ) {
        TicketSoporte ticketActualizado = ticketService.cambiarEstado(idTicket, request.getEstado());
        return agregarLinksTicket(ticketActualizado);
    }

    @PatchMapping("/{idTicket}/asignar")
    @Operation(
            summary = "Asignar responsable a un ticket",
            description = """
                    Asigna un usuario responsable al ticket.

                    Al asignar un responsable:
                    - Se registra idUsuarioAsignado.
                    - El estado del ticket cambia a EN_REVISION.
                    - Se actualiza la fecha de modificación.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Responsable asignado correctamente"),
            @ApiResponse(responseCode = "400", description = "No se puede asignar responsable al ticket"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public EntityModel<TicketSoporte> asignarResponsable(
            @Parameter(
                    description = "Identificador del ticket al que se asignará responsable",
                    example = "1",
                    required = true
            )
            @PathVariable Long idTicket,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Identificador del usuario responsable asignado",
                    required = true
            )
            @Valid @RequestBody AsignarTicketRequest request
    ) {
        TicketSoporte ticketAsignado = ticketService.asignarResponsable(
                idTicket,
                request.getIdUsuarioAsignado()
        );

        return agregarLinksTicket(ticketAsignado);
    }

    @PatchMapping("/{idTicket}/cerrar")
    @Operation(
            summary = "Cerrar ticket",
            description = """
                    Cambia el estado del ticket a CERRADO.

                    Al cerrar el ticket se registra la fecha de cierre.
                    No se puede cerrar un ticket que ya esté cerrado o cancelado.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket cerrado correctamente"),
            @ApiResponse(responseCode = "400", description = "No se puede cerrar el ticket"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public EntityModel<TicketSoporte> cerrarTicket(
            @Parameter(
                    description = "Identificador del ticket que será cerrado",
                    example = "1",
                    required = true
            )
            @PathVariable Long idTicket
    ) {
        TicketSoporte ticketCerrado = ticketService.cerrarTicket(idTicket);
        return agregarLinksTicket(ticketCerrado);
    }

    @PatchMapping("/{idTicket}/cancelar")
    @Operation(
            summary = "Cancelar ticket",
            description = """
                    Cambia el estado del ticket a CANCELADO.

                    Al cancelar el ticket se registra la fecha de cierre.
                    No se puede cancelar un ticket que ya esté cerrado o cancelado.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket cancelado correctamente"),
            @ApiResponse(responseCode = "400", description = "No se puede cancelar el ticket"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public EntityModel<TicketSoporte> cancelarTicket(
            @Parameter(
                    description = "Identificador del ticket que será cancelado",
                    example = "1",
                    required = true
            )
            @PathVariable Long idTicket
    ) {
        TicketSoporte ticketCancelado = ticketService.cancelarTicket(idTicket);
        return agregarLinksTicket(ticketCancelado);
    }

    @DeleteMapping("/{idTicket}")
    @Operation(
            summary = "Eliminar ticket",
            description = """
                    Elimina un ticket de soporte según su identificador.

                    Si el ticket no existe, se responderá con error 404.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ticket eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Void> eliminarTicket(
            @Parameter(
                    description = "Identificador del ticket que será eliminado",
                    example = "1",
                    required = true
            )
            @PathVariable Long idTicket
    ) {
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