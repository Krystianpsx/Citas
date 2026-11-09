package com.clinica.citas.aplicacion.puertos.entrada;

import com.clinica.citas.aplicacion.consultas.CitaDetallada;

public interface CancelarCitaCasoDeUso {

    CitaDetallada ejecutar(Comando comando);

    record Comando(String citaId, String motivo) {
    }
}
