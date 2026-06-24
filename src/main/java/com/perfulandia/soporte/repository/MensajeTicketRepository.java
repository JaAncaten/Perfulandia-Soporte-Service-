package com.perfulandia.soporte.repository;

import com.perfulandia.soporte.model.MensajeTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeTicketRepository extends JpaRepository<MensajeTicket, Long> {

    List<MensajeTicket> findByTicketIdTicket(Long idTicket);
}