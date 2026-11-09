package com.clinica.citas.aplicacion.consultas;

import com.clinica.citas.dominio.modelo.Cita;
import com.clinica.citas.dominio.modelo.EstadoCita;
import java.time.LocalDate;

/**
 * Filtros opcionales para buscar citas. Un criterio en null significa "sin filtrar por ese campo".
 * Se construye de forma legible: {@code todas().delDia(hoy).delMedico(id)}.
 */
public record CriteriosDeBusquedaDeCitas(LocalDate dia, String medicoId, String pacienteId, EstadoCita estado) {

    public static CriteriosDeBusquedaDeCitas todas() {
        return new CriteriosDeBusquedaDeCitas(null, null, null, null);
    }

    public CriteriosDeBusquedaDeCitas delDia(LocalDate nuevoDia) {
        return new CriteriosDeBusquedaDeCitas(nuevoDia, medicoId, pacienteId, estado);
    }

    public CriteriosDeBusquedaDeCitas delMedico(String nuevoMedicoId) {
        return new CriteriosDeBusquedaDeCitas(dia, nuevoMedicoId, pacienteId, estado);
    }

    public CriteriosDeBusquedaDeCitas delPaciente(String nuevoPacienteId) {
        return new CriteriosDeBusquedaDeCitas(dia, medicoId, nuevoPacienteId, estado);
    }

    public CriteriosDeBusquedaDeCitas conEstado(EstadoCita nuevoEstado) {
        return new CriteriosDeBusquedaDeCitas(dia, medicoId, pacienteId, nuevoEstado);
    }

    public boolean coincideCon(Cita cita) {
        return (dia == null || cita.inicio().toLocalDate().equals(dia))
                && (medicoId == null || cita.medicoId().equals(medicoId))
                && (pacienteId == null || cita.pacienteId().equals(pacienteId))
                && (estado == null || cita.estado() == estado);
    }
}
