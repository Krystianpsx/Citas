package com.clinica.citas.infraestructura.web.dto;

import com.clinica.citas.dominio.modelo.Paciente;
import com.clinica.citas.infraestructura.web.FechasEnJson;

public record PacienteRespuesta(
        String id, String nombre, String documento, String email, String telefono, String fechaNacimiento) {

    public static PacienteRespuesta desde(Paciente paciente) {
        return new PacienteRespuesta(
                paciente.id(),
                paciente.nombre(),
                paciente.documento().numero(),
                paciente.email().valor(),
                paciente.telefono().orElse(null),
                FechasEnJson.texto(paciente.fechaNacimiento().orElse(null)));
    }
}
