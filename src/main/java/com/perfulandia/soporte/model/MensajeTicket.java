package com.perfulandia.soporte.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes_ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMensaje;

    @ManyToOne
    @JoinColumn(name = "id_ticket", nullable = false)
    @JsonBackReference
    private TicketSoporte ticket;

    @NotNull(message = "El idUsuario es obligatorio")
    @Column(nullable = false)
    private Long idUsuario;

    @NotBlank(message = "El mensaje es obligatorio")
    @Column(nullable = false, length = 1000)
    private String mensaje;

    private LocalDateTime fechaEnvio;

    @NotNull(message = "El tipoAutor es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAutorMensaje tipoAutor;

    @PrePersist
    public void prePersist() {
        this.fechaEnvio = LocalDateTime.now();
    }
}