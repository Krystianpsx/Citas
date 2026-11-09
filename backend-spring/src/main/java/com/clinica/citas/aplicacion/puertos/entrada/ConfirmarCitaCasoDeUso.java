package com.clinica.citas.aplicacion.puertos.entrada;

import com.clinica.citas.aplicacion.consultas.CitaDetallada;

public interface ConfirmarCitaCasoDeUso {

    CitaDetallada ejecutar(String citaId);
}
