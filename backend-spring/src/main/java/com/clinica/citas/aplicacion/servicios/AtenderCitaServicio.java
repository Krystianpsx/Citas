package com.clinica.citas.aplicacion.servicios;

import com.clinica.citas.aplicacion.consultas.CitaDetallada;
import com.clinica.citas.aplicacion.puertos.entrada.AtenderCitaCasoDeUso;
import com.clinica.citas.aplicacion.puertos.salida.Reloj;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeCitas;
import com.clinica.citas.dominio.modelo.Cita;

public class AtenderCitaServicio extends OperacionSobreCitaExistente implements AtenderCitaCasoDeUso {

    private final Reloj reloj;

    public AtenderCitaServicio(RepositorioDeCitas citas, DetalladorDeCitas detallador, Reloj reloj) {
        super(citas, detallador);
        this.reloj = reloj;
    }

    @Override
    public CitaDetallada ejecutar(String citaId) {
        Cita cita = obtenerCita(citaId);
        cita.marcarComoAtendida(reloj.ahora());
        return guardarYDetallar(cita);
    }
}
