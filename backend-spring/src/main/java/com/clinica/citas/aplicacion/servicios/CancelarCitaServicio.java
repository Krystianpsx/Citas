package com.clinica.citas.aplicacion.servicios;

import com.clinica.citas.aplicacion.consultas.CitaDetallada;
import com.clinica.citas.aplicacion.puertos.entrada.CancelarCitaCasoDeUso;
import com.clinica.citas.aplicacion.puertos.salida.Notificador;
import com.clinica.citas.aplicacion.puertos.salida.Reloj;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeCitas;
import com.clinica.citas.dominio.modelo.Cita;

public class CancelarCitaServicio extends OperacionSobreCitaExistente implements CancelarCitaCasoDeUso {

    private final Reloj reloj;
    private final Notificador notificador;

    public CancelarCitaServicio(RepositorioDeCitas citas, DetalladorDeCitas detallador,
                                Reloj reloj, Notificador notificador) {
        super(citas, detallador);
        this.reloj = reloj;
        this.notificador = notificador;
    }

    @Override
    public CitaDetallada ejecutar(Comando comando) {
        Cita cita = obtenerCita(comando.citaId());
        cita.cancelar(comando.motivo(), reloj.ahora());
        CitaDetallada detalle = guardarYDetallar(cita);
        notificador.notificarCancelacion(detalle);
        return detalle;
    }
}
