package com.clinica.citas.aplicacion.puertos.salida;

import com.clinica.citas.aplicacion.consultas.CriteriosDeBusquedaDeCitas;
import com.clinica.citas.dominio.modelo.Cita;
import java.util.List;
import java.util.Optional;

/**
 * Las búsquedas devuelven copias: modificar una cita recuperada no altera lo guardado
 * hasta que se llame de nuevo a guardar(). Los resultados vienen ordenados por hora de inicio.
 */
public interface RepositorioDeCitas {

    void guardar(Cita cita);

    Optional<Cita> buscarPorId(String citaId);

    List<Cita> buscar(CriteriosDeBusquedaDeCitas criterios);
}
