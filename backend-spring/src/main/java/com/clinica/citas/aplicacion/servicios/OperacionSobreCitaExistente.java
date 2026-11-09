package com.clinica.citas.aplicacion.servicios;

import com.clinica.citas.aplicacion.consultas.CitaDetallada;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeCitas;
import com.clinica.citas.dominio.excepciones.EntidadNoEncontrada;
import com.clinica.citas.dominio.modelo.Cita;

/**
 * Comportamiento común de los casos de uso que parten de una cita ya guardada.
 *
 * <p>Refactorización <em>Pull Up Method</em>: buscar la cita, fallar si no existe,
 * guardarla y devolverla detallada estaba copiado en cuatro clases.
 */
abstract class OperacionSobreCitaExistente {

    private final RepositorioDeCitas citas;
    private final DetalladorDeCitas detallador;

    protected OperacionSobreCitaExistente(RepositorioDeCitas citas, DetalladorDeCitas detallador) {
        this.citas = citas;
        this.detallador = detallador;
    }

    protected Cita obtenerCita(String citaId) {
        return citas.buscarPorId(citaId).orElseThrow(() -> EntidadNoEncontrada.conId("la cita", citaId));
    }

    protected CitaDetallada guardarYDetallar(Cita cita) {
        citas.guardar(cita);
        return detallador.detallar(cita);
    }
}
