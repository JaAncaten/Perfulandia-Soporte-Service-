package com.perfulandia.soporte.controller;

import com.perfulandia.soporte.dto.AsignarTicketRequest;
import com.perfulandia.soporte.dto.CambiarEstadoTicketRequest;
import com.perfulandia.soporte.exception.GlobalExceptionHandler;
import com.perfulandia.soporte.exception.RecursoNoEncontradoException;
import com.perfulandia.soporte.model.EstadoTicket;
import com.perfulandia.soporte.model.PrioridadTicket;
import com.perfulandia.soporte.model.TicketSoporte;
import com.perfulandia.soporte.service.TicketSoporteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketSoporteController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class TicketSoporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TicketSoporteService ticketService;

    @Test
    void listarTickets_deberiaRetornarListaConHateoas() throws Exception {
        TicketSoporte ticket = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Problema con pedido")
                .descripcion("Mi pedido no llegó")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        when(ticketService.listarTickets()).thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void buscarPorId_deberiaRetornarTicketCuandoExiste() throws Exception {
        TicketSoporte ticket = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Problema con pedido")
                .descripcion("Mi pedido no llegó")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        when(ticketService.buscarPorId(1L)).thenReturn(ticket);

        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTicket", is(1)))
                .andExpect(jsonPath("$.idUsuario", is(10)))
                .andExpect(jsonPath("$.asunto", is("Problema con pedido")))
                .andExpect(jsonPath("$.estado", is("ABIERTO")))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.todos-los-tickets").exists())
                .andExpect(jsonPath("$._links.mensajes-del-ticket").exists());
    }

    @Test
    void buscarPorId_deberiaRetornarNotFoundCuandoNoExiste() throws Exception {
        when(ticketService.buscarPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Ticket no encontrado con ID: 99"));

        mockMvc.perform(get("/api/tickets/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.mensaje", is("Ticket no encontrado con ID: 99")));
    }

    @Test
    void listarPorUsuario_deberiaRetornarTicketsDelUsuario() throws Exception {
        TicketSoporte ticket = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Problema con pedido")
                .descripcion("Mi pedido no llegó")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        when(ticketService.listarPorUsuario(10L)).thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/tickets/usuario/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.todos-los-tickets").exists());
    }

    @Test
    void crearTicket_deberiaRetornarTicketCreado() throws Exception {
        TicketSoporte request = TicketSoporte.builder()
                .idUsuario(10L)
                .asunto("Consulta")
                .descripcion("Necesito ayuda")
                .prioridad(PrioridadTicket.MEDIA)
                .build();

        TicketSoporte creado = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Consulta")
                .descripcion("Necesito ayuda")
                .prioridad(PrioridadTicket.MEDIA)
                .estado(EstadoTicket.ABIERTO)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        when(ticketService.crearTicket(any(TicketSoporte.class))).thenReturn(creado);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTicket", is(1)))
                .andExpect(jsonPath("$.idUsuario", is(10)))
                .andExpect(jsonPath("$.estado", is("ABIERTO")))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void actualizarTicket_deberiaRetornarTicketActualizado() throws Exception {
        TicketSoporte request = TicketSoporte.builder()
                .idUsuario(10L)
                .asunto("Asunto actualizado")
                .descripcion("Descripción actualizada")
                .prioridad(PrioridadTicket.URGENTE)
                .build();

        TicketSoporte actualizado = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Asunto actualizado")
                .descripcion("Descripción actualizada")
                .prioridad(PrioridadTicket.URGENTE)
                .estado(EstadoTicket.ABIERTO)
                .build();

        when(ticketService.actualizarTicket(eq(1L), any(TicketSoporte.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/tickets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.asunto", is("Asunto actualizado")))
                .andExpect(jsonPath("$.prioridad", is("URGENTE")))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void cambiarEstado_deberiaRetornarTicketConNuevoEstado() throws Exception {
        CambiarEstadoTicketRequest request = new CambiarEstadoTicketRequest(EstadoTicket.EN_REVISION);

        TicketSoporte actualizado = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.EN_REVISION)
                .build();

        when(ticketService.cambiarEstado(1L, EstadoTicket.EN_REVISION)).thenReturn(actualizado);

        mockMvc.perform(patch("/api/tickets/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("EN_REVISION")))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void asignarResponsable_deberiaRetornarTicketAsignado() throws Exception {
        AsignarTicketRequest request = new AsignarTicketRequest(50L);

        TicketSoporte asignado = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .idUsuarioAsignado(50L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.EN_REVISION)
                .build();

        when(ticketService.asignarResponsable(1L, 50L)).thenReturn(asignado);

        mockMvc.perform(patch("/api/tickets/1/asignar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuarioAsignado", is(50)))
                .andExpect(jsonPath("$.estado", is("EN_REVISION")))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void cerrarTicket_deberiaRetornarTicketCerrado() throws Exception {
        TicketSoporte cerrado = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.CERRADO)
                .fechaCierre(LocalDateTime.now())
                .build();

        when(ticketService.cerrarTicket(1L)).thenReturn(cerrado);

        mockMvc.perform(patch("/api/tickets/1/cerrar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CERRADO")))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void cancelarTicket_deberiaRetornarTicketCancelado() throws Exception {
        TicketSoporte cancelado = TicketSoporte.builder()
                .idTicket(1L)
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.CANCELADO)
                .fechaCierre(LocalDateTime.now())
                .build();

        when(ticketService.cancelarTicket(1L)).thenReturn(cancelado);

        mockMvc.perform(patch("/api/tickets/1/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CANCELADO")))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void eliminarTicket_deberiaRetornarNoContent() throws Exception {
        doNothing().when(ticketService).eliminarTicket(1L);

        mockMvc.perform(delete("/api/tickets/1"))
                .andExpect(status().isNoContent());
    }


    @Test
     void listarPorUsuarioAsignado_deberiaRetornarTicketsAsignadosConHateoas() throws Exception {
        Long idUsuarioAsignado = 50L;

        TicketSoporte ticket = TicketSoporte.builder()
            .idTicket(1L)
            .idUsuario(10L)
            .idUsuarioAsignado(idUsuarioAsignado)
            .asunto("Problema con pedido")
            .descripcion("El pedido no llegó")
            .prioridad(PrioridadTicket.ALTA)
            .estado(EstadoTicket.EN_REVISION)
            .build();

        when(ticketService.listarPorUsuarioAsignado(idUsuarioAsignado))
                .thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/tickets/asignado/50"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded").exists())
            .andExpect(jsonPath("$._links.self").exists())
            .andExpect(jsonPath("$._links.todos-los-tickets").exists());
}


    
}