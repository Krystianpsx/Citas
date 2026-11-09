package com.clinica.citas.infraestructura.web.dto;

import com.clinica.citas.dominio.modelo.Medico;

public record MedicoRespuesta(
        String id, String nombre, EspecialidadRespuesta especialidad, String registroProfesional, String email) {

    public static MedicoRespuesta desde(Medico medico) {
        return new MedicoRespuesta(
                medico.id(),
                medico.nombre(),
                EspecialidadRespuesta.desde(medico.especialidad()),
                medico.registroProfesional(),
                medico.email().valor());
    }
}
