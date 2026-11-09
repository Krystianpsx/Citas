package com.clinica.citas.infraestructura.web.dto;

import com.clinica.citas.dominio.modelo.HorarioDeAtencion;
import java.time.DayOfWeek;
import java.util.List;

/** diasHabiles usa 0 = lunes, el mismo contrato que la versión Flask y que espera el frontend. */
public record HorarioRespuesta(
        String apertura, String cierre, long duracionCitaMinutos, List<Integer> diasHabiles, String descripcion) {

    public static HorarioRespuesta desde(HorarioDeAtencion horario) {
        return new HorarioRespuesta(
                horario.apertura().toString(),
                horario.cierre().toString(),
                horario.minutosPorCita(),
                horario.diasHabiles().stream().map(DayOfWeek::getValue).map(numeroIso -> numeroIso - 1).sorted().toList(),
                horario.descripcion());
    }
}
