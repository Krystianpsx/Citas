package com.clinica.citas.infraestructura.web.dto;

import com.clinica.citas.dominio.modelo.Especialidad;

public record EspecialidadRespuesta(String codigo, String nombre) {

    public static EspecialidadRespuesta desde(Especialidad especialidad) {
        return new EspecialidadRespuesta(especialidad.name(), especialidad.nombreLegible());
    }
}
