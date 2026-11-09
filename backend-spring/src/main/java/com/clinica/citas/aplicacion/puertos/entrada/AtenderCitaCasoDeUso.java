package com.clinica.citas.aplicacion.puertos.entrada;

import com.clinica.citas.aplicacion.consultas.CitaDetallada;

public interface AtenderCitaCasoDeUso {

    CitaDetallada ejecutar(String citaId);
}
