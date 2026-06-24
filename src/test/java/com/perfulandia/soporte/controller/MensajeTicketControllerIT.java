package com.perfulandia.soporte.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.perfulandia.soporte.model.EstadoTicket;
import com.perfulandia.soporte.model.MensajeTicket;
import com.perfulandia.soporte.model.PrioridadTicket;
import com.perfulandia.soporte.model.TicketSoporte;
import com.perfulandia.soporte.model.TipoAutorMensaje;
import com.perfulandia.soporte.repository.MensajeTicketRepository;
import com.perfulandia.soporte.repository.TicketSoporteRepository;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MensajeTicketControllerIT {

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
    void agregarMensaje_deberiaGuardarMensajeEnTicket() throws Exception {
        TicketSoporte ticket = TicketSoporte.builder()
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        TicketSoporte ticketGuardado = ticketRepository.save(ticket);

        MensajeTicket mensaje = MensajeTicket.builder()
                .idUsuario(10L)
                .mensaje("Necesito ayuda")
                .tipoAutor(TipoAutorMensaje.CLIENTE)
                .build();

        mockMvc.perform(post("/api/tickets/" + ticketGuardado.getIdTicket() + "/mensajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mensaje)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMensaje").exists())
                .andExpect(jsonPath("$.idUsuario", is(10)))
                .andExpect(jsonPath("$.mensaje", is("Necesito ayuda")))
                .andExpect(jsonPath("$.tipoAutor", is("CLIENTE")))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.ticket").exists());
    }

    @Test
    void listarMensajes_deberiaRetornarMensajesDelTicket() throws Exception {
        TicketSoporte ticket = TicketSoporte.builder()
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.MEDIA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        TicketSoporte ticketGuardado = ticketRepository.save(ticket);

        MensajeTicket mensaje = MensajeTicket.builder()
                .ticket(ticketGuardado)
                .idUsuario(10L)
                .mensaje("Mensaje de prueba")
                .tipoAutor(TipoAutorMensaje.CLIENTE)
                .build();

        mensajeRepository.save(mensaje);

        mockMvc.perform(get("/api/tickets/" + ticketGuardado.getIdTicket() + "/mensajes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.ticket").exists());
    }

    @Test
    void buscarMensajePorId_deberiaRetornarMensajeExistente() throws Exception {
        TicketSoporte ticket = TicketSoporte.builder()
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.MEDIA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        TicketSoporte ticketGuardado = ticketRepository.save(ticket);

        MensajeTicket mensaje = MensajeTicket.builder()
                .ticket(ticketGuardado)
                .idUsuario(10L)
                .mensaje("Mensaje de prueba")
                .tipoAutor(TipoAutorMensaje.CLIENTE)
                .build();

        MensajeTicket mensajeGuardado = mensajeRepository.save(mensaje);

        mockMvc.perform(get("/api/tickets/" + ticketGuardado.getIdTicket()
                        + "/mensajes/" + mensajeGuardado.getIdMensaje()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMensaje").value(mensajeGuardado.getIdMensaje()))
                .andExpect(jsonPath("$.mensaje", is("Mensaje de prueba")))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void buscarMensajePorId_deberiaRetornarNotFoundSiNoExiste() throws Exception {
        mockMvc.perform(get("/api/tickets/1/mensajes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.mensaje", is("Mensaje no encontrado con ID: 999")));
    }

    @Test
    void agregarMensaje_deberiaRetornarBadRequestSiTicketEstaCerrado() throws Exception {
        TicketSoporte ticket = TicketSoporte.builder()
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.ALTA)
                .estado(EstadoTicket.CERRADO)
                .build();

        TicketSoporte ticketGuardado = ticketRepository.save(ticket);

        MensajeTicket mensaje = MensajeTicket.builder()
                .idUsuario(10L)
                .mensaje("No se debería agregar")
                .tipoAutor(TipoAutorMensaje.CLIENTE)
                .build();

        mockMvc.perform(post("/api/tickets/" + ticketGuardado.getIdTicket() + "/mensajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mensaje)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.mensaje", is("No se pueden agregar mensajes a un ticket cerrado o cancelado")));
    }

    @Test
    void eliminarMensaje_deberiaEliminarMensajeExistente() throws Exception {
        TicketSoporte ticket = TicketSoporte.builder()
                .idUsuario(10L)
                .asunto("Problema")
                .descripcion("Descripción")
                .prioridad(PrioridadTicket.MEDIA)
                .estado(EstadoTicket.ABIERTO)
                .build();

        TicketSoporte ticketGuardado = ticketRepository.save(ticket);

        MensajeTicket mensaje = MensajeTicket.builder()
                .ticket(ticketGuardado)
                .idUsuario(10L)
                .mensaje("Mensaje a eliminar")
                .tipoAutor(TipoAutorMensaje.CLIENTE)
                .build();

        MensajeTicket mensajeGuardado = mensajeRepository.save(mensaje);

        mockMvc.perform(delete("/api/tickets/" + ticketGuardado.getIdTicket()
                        + "/mensajes/" + mensajeGuardado.getIdMensaje()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tickets/" + ticketGuardado.getIdTicket()
                        + "/mensajes/" + mensajeGuardado.getIdMensaje()))
                .andExpect(status().isNotFound());
    }
}