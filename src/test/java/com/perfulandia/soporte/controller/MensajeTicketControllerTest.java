package com.perfulandia.soporte.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.perfulandia.soporte.exception.GlobalExceptionHandler;
import com.perfulandia.soporte.exception.RecursoNoEncontradoException;
import com.perfulandia.soporte.exception.ReglaNegocioException;
import com.perfulandia.soporte.model.MensajeTicket;
import com.perfulandia.soporte.model.TipoAutorMensaje;
import com.perfulandia.soporte.service.MensajeTicketService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(MensajeTicketController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class MensajeTicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MensajeTicketService mensajeService;

    @Test
    void listarMensajes_deberiaRetornarListaConHateoas() throws Exception {
        MensajeTicket mensaje = MensajeTicket.builder()
                .idMensaje(1L)
                .idUsuario(10L)
                .mensaje("Necesito ayuda")
                .fechaEnvio(LocalDateTime.now())
                .tipoAutor(TipoAutorMensaje.CLIENTE)
                .build();

        when(mensajeService.listarMensajesPorTicket(1L)).thenReturn(List.of(mensaje));

        mockMvc.perform(get("/api/tickets/1/mensajes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.ticket").exists());
    }

    @Test
    void agregarMensaje_deberiaRetornarMensajeCreado() throws Exception {
        MensajeTicket request = MensajeTicket.builder()
                .idUsuario(10L)
                .mensaje("Necesito ayuda")
                .tipoAutor(TipoAutorMensaje.CLIENTE)
                .build();

        MensajeTicket creado = MensajeTicket.builder()
                .idMensaje(1L)
                .idUsuario(10L)
                .mensaje("Necesito ayuda")
                .fechaEnvio(LocalDateTime.now())
                .tipoAutor(TipoAutorMensaje.CLIENTE)
                .build();

        when(mensajeService.agregarMensaje(any(Long.class), any(MensajeTicket.class))).thenReturn(creado);

        mockMvc.perform(post("/api/tickets/1/mensajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMensaje", is(1)))
                .andExpect(jsonPath("$.idUsuario", is(10)))
                .andExpect(jsonPath("$.mensaje", is("Necesito ayuda")))
                .andExpect(jsonPath("$.tipoAutor", is("CLIENTE")))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.ticket").exists());
    }

    @Test
    void agregarMensaje_deberiaRetornarBadRequestSiTicketEstaCerrado() throws Exception {
        MensajeTicket request = MensajeTicket.builder()
                .idUsuario(10L)
                .mensaje("No debería agregarse")
                .tipoAutor(TipoAutorMensaje.CLIENTE)
                .build();

        when(mensajeService.agregarMensaje(any(Long.class), any(MensajeTicket.class)))
                .thenThrow(new ReglaNegocioException("No se pueden agregar mensajes a un ticket cerrado o cancelado"));

        mockMvc.perform(post("/api/tickets/1/mensajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.mensaje", is("No se pueden agregar mensajes a un ticket cerrado o cancelado")));
    }

    @Test
    void buscarMensajePorId_deberiaRetornarMensajeCuandoExiste() throws Exception {
        MensajeTicket mensaje = MensajeTicket.builder()
                .idMensaje(1L)
                .idUsuario(10L)
                .mensaje("Necesito ayuda")
                .fechaEnvio(LocalDateTime.now())
                .tipoAutor(TipoAutorMensaje.CLIENTE)
                .build();

        when(mensajeService.buscarMensajePorId(1L)).thenReturn(mensaje);

        mockMvc.perform(get("/api/tickets/1/mensajes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMensaje", is(1)))
                .andExpect(jsonPath("$.mensaje", is("Necesito ayuda")))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.ticket").exists());
    }

    @Test
    void buscarMensajePorId_deberiaRetornarNotFoundCuandoNoExiste() throws Exception {
        when(mensajeService.buscarMensajePorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Mensaje no encontrado con ID: 99"));

        mockMvc.perform(get("/api/tickets/1/mensajes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.mensaje", is("Mensaje no encontrado con ID: 99")));
    }

    @Test
    void eliminarMensaje_deberiaRetornarNoContent() throws Exception {
        doNothing().when(mensajeService).eliminarMensaje(1L);

        mockMvc.perform(delete("/api/tickets/1/mensajes/1"))
                .andExpect(status().isNoContent());
    }
}