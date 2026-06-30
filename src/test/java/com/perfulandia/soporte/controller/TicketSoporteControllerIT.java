package com.perfulandia.soporte.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.perfulandia.soporte.dto.AsignarTicketRequest;
import com.perfulandia.soporte.dto.CambiarEstadoTicketRequest;
import com.perfulandia.soporte.model.EstadoTicket;
import com.perfulandia.soporte.model.PrioridadTicket;
import com.perfulandia.soporte.model.TicketSoporte;
import com.perfulandia.soporte.repository.MensajeTicketRepository;
import com.perfulandia.soporte.repository.TicketSoporteRepository;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TicketSoporteControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketSoporteRepository ticketRepository;

    @Autowired
    private MensajeTicketRepository mensajeRepository;

    @BeforeEach
    void limpiarBaseDatos() {
        mensajeRepository.deleteAll();
        ticketRepository.deleteAll();
    }

    @Test
    void crearTicket_deberiaGuardarTicketEnBaseH2() throws Exception {
        TicketSoporte ticket = TicketSoporte.builder()
                .idUsuario(10L)
                .asunto("Problema con pedido")
                .descripcion("Mi pedido no llegó")
                .prioridad(PrioridadTicket.ALTA)
                .build();

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticket)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTicket").exists())
                .andExpect(jsonPath("$.idUsuario", is(10)))
                .andExpect(jsonPath("$.asunto", is("Problema con pedido")))
                .andExpect(jsonPath("$.estado", is("ABIERTO")))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void listarTickets_deberiaRetornarTicketsGuardados() throws Exception {
        TicketSoporte ticket = TicketSoporte.builder()
                .idUsuario(10L)
                .asunto("Consulta")
                .descripcion("Necesito ayuda")
                .prioridad(PrioridadTicket.MEDIA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        ticketRepository.save(ticket);

        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void buscarPorId_deberiaRetornarTicketExistente() throws Exception {
        TicketSoporte ticket = TicketSoporte.builder()
                .idUsuario(20L)
                .asunto("Problema con pago")
                .descripcion("No puedo pagar")
                .prioridad(PrioridadTicket.URGENTE)
                .estado(EstadoTicket.ABIERTO)
                .build();

        TicketSoporte guardado = ticketRepository.save(ticket);

        mockMvc.perform(get("/api/tickets/" + guardado.getIdTicket()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTicket").value(guardado.getIdTicket()))
                .andExpect(jsonPath("$.idUsuario", is(20)))
                .andExpect(jsonPath("$.prioridad", is("URGENTE")))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void buscarPorId_deberiaRetornarNotFoundSiNoExiste() throws Exception {
        mockMvc.perform(get("/api/tickets/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.mensaje", is("Ticket no encontrado con ID: 999")));
    }

    @Test
    void listarPorUsuario_deberiaRetornarTicketsDelUsuario() throws Exception {
        TicketSoporte ticket = TicketSoporte.builder()
                .idUsuario(30L)
                .asunto("Ticket del usuario")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.BAJA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        ticketRepository.save(ticket);

        mockMvc.perform(get("/api/tickets/usuario/30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void actualizarTicket_deberiaModificarDatos() throws Exception {
        TicketSoporte ticket = TicketSoporte.builder()
                .idUsuario(10L)
                .asunto("Asunto inicial")
                .descripcion("Descripción inicial")
                .prioridad(PrioridadTicket.MEDIA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        TicketSoporte guardado = ticketRepository.save(ticket);

        TicketSoporte actualizado = TicketSoporte.builder()
                .idUsuario(10L)
                .asunto("Asunto actualizado")
                .descripcion("Descripción actualizada")
                .prioridad(PrioridadTicket.URGENTE)
                .build();

        mockMvc.perform(put("/api/tickets/" + guardado.getIdTicket())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.asunto", is("Asunto actualizado")))
                .andExpect(jsonPath("$.prioridad", is("URGENTE")));
    }

    @Test
    void cambiarEstado_deberiaModificarEstado() throws Exception {
        TicketSoporte ticket = TicketSoporte.builder()
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        TicketSoporte guardado = ticketRepository.save(ticket);

        CambiarEstadoTicketRequest request = new CambiarEstadoTicketRequest(EstadoTicket.EN_REVISION);

        mockMvc.perform(patch("/api/tickets/" + guardado.getIdTicket() + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("EN_REVISION")));
    }

    @Test
    void asignarResponsable_deberiaAsignarUsuarioYCambiarEstado() throws Exception {
        TicketSoporte ticket = TicketSoporte.builder()
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        TicketSoporte guardado = ticketRepository.save(ticket);

        AsignarTicketRequest request = new AsignarTicketRequest(50L);

        mockMvc.perform(patch("/api/tickets/" + guardado.getIdTicket() + "/asignar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuarioAsignado", is(50)))
                .andExpect(jsonPath("$.estado", is("EN_REVISION")));
    }

    @Test
    void cerrarTicket_deberiaCerrarTicket() throws Exception {
        TicketSoporte ticket = TicketSoporte.builder()
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        TicketSoporte guardado = ticketRepository.save(ticket);

        mockMvc.perform(patch("/api/tickets/" + guardado.getIdTicket() + "/cerrar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CERRADO")))
                .andExpect(jsonPath("$.fechaCierre").exists());
    }

    @Test
    void cancelarTicket_deberiaCancelarTicket() throws Exception {
        TicketSoporte ticket = TicketSoporte.builder()
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        TicketSoporte guardado = ticketRepository.save(ticket);

        mockMvc.perform(patch("/api/tickets/" + guardado.getIdTicket() + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CANCELADO")))
                .andExpect(jsonPath("$.fechaCierre").exists());
    }

    @Test
    void eliminarTicket_deberiaEliminarTicket() throws Exception {
        TicketSoporte ticket = TicketSoporte.builder()
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        TicketSoporte guardado = ticketRepository.save(ticket);

        mockMvc.perform(delete("/api/tickets/" + guardado.getIdTicket()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tickets/" + guardado.getIdTicket()))
                .andExpect(status().isNotFound());
    }


    
}