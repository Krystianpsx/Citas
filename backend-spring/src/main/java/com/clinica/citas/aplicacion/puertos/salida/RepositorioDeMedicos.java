package com.clinica.citas.aplicacion.puertos.salida;

import com.clinica.citas.dominio.modelo.Especialidad;
import com.clinica.citas.dominio.modelo.Medico;
import java.util.List;
import java.util.Optional;

/** Los listados vienen ordenados por nombre. */
public interface RepositorioDeMedicos {

    void guardar(Medico medico);

    Optional<Medico> buscarPorId(String medicoId);

    Optional<Medico> buscarPorRegistro(String registroProfesional);

    List<Medico> listar();

    List<Medico> listarPorEspecialidad(Especialidad especialidad);
}
