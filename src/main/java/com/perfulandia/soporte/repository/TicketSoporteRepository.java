package com.perfulandia.soporte.repository;

import com.perfulandia.soporte.model.TicketSoporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketSoporteRepository extends JpaRepository<TicketSoporte, Long> {

    List<TicketSoporte> findByIdUsuario(Long idUsuario);

    List<TicketSoporte> findByIdUsuarioAsignado(Long idUsuarioAsignado);
}