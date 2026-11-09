package com.clinica.citas.aplicacion.puertos.entrada;

import com.clinica.citas.aplicacion.consultas.CitaDetallada;
import java.time.LocalDateTime;

public interface AgendarCitaCasoDeUso {

    CitaDetallada ejecutar(Comando comando);

    record Comando(String pacienteId, String medicoId, LocalDateTime inicio, String motivo) {
    }
}
