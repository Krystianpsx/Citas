package com.clinica.citas.aplicacion.puertos.entrada;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Horas de inicio libres de un médico en un día. */
public interface ConsultarDisponibilidadCasoDeUso {

    List<LocalDateTime> ejecutar(Comando comando);

    record Comando(String medicoId, LocalDate dia) {
    }
}
