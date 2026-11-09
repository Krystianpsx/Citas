package com.clinica.citas.aplicacion.puertos.entrada;

import com.clinica.citas.aplicacion.consultas.CitaDetallada;
import java.time.LocalDateTime;

public interface ReprogramarCitaCasoDeUso {

    CitaDetallada ejecutar(Comando comando);

    record Comando(String citaId, LocalDateTime nuevoInicio) {
    }
}
