package com.clinica.citas.aplicacion.servicios;

import com.clinica.citas.aplicacion.consultas.CitaDetallada;
import com.clinica.citas.aplicacion.puertos.entrada.ConfirmarCitaCasoDeUso;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeCitas;
import com.clinica.citas.dominio.modelo.Cita;

public class ConfirmarCitaServicio extends OperacionSobreCitaExistente implements ConfirmarCitaCasoDeUso {

    public ConfirmarCitaServicio(RepositorioDeCitas citas, DetalladorDeCitas detallador) {
        super(citas, detallador);
    }

    @Override
    public CitaDetallada ejecutar(String citaId) {
        Cita cita = obtenerCita(citaId);
        cita.confirmar();
        return guardarYDetallar(cita);
    }
}
