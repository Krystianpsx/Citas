package com.clinica.citas.infraestructura.web.dto;

import com.clinica.citas.aplicacion.consultas.CitaDetallada;
import com.clinica.citas.dominio.modelo.Cita;
import com.clinica.citas.infraestructura.web.FechasEnJson;

public record CitaRespuesta(
        String id,
        String inicio,
        String fin,
        String motivo,
        String estado,
        String motivoCancelacion,
        PacienteResumido paciente,
        MedicoResumido medico) {

    public record PacienteResumido(String id, String nombre) {
    }

    public record MedicoResumido(String id, String nombre, EspecialidadRespuesta especialidad) {
    }

    public static CitaRespuesta desde(CitaDetallada detalle) {
        Cita cita = detalle.cita();
        return new CitaRespuesta(
                cita.id(),
                FechasEnJson.texto(cita.inicio()),
                FechasEnJson.texto(cita.fin()),
                cita.motivo(),
                cita.estado().name(),
                cita.motivoCancelacion().orElse(null),
                new PacienteResumido(detalle.paciente().id(), detalle.paciente().nombre()),
                new MedicoResumido(
                        detalle.medico().id(),
                        detalle.medico().nombre(),
                        EspecialidadRespuesta.desde(detalle.medico().especialidad())));
    }
}
