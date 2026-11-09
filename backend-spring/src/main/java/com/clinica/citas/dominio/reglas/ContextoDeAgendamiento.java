package com.clinica.citas.dominio.reglas;

import com.clinica.citas.dominio.modelo.Cita;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Información que necesitan las reglas. Llega ya consultada, así las reglas son
 * funciones puras del dominio y no dependen de repositorios.
 */
public record ContextoDeAgendamiento(LocalDateTime ahora, List<Cita> agendaDelMedico, List<Cita> agendaDelPaciente) {

    public ContextoDeAgendamiento {
        agendaDelMedico = List.copyOf(agendaDelMedico);
        agendaDelPaciente = List.copyOf(agendaDelPaciente);
    }
}
