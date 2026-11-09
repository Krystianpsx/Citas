package com.clinica.citas.aplicacion.puertos.salida;

import com.clinica.citas.dominio.modelo.DocumentoIdentidad;
import com.clinica.citas.dominio.modelo.Paciente;
import java.util.List;
import java.util.Optional;

/**
 * Contrato que toda implementación debe respetar (Sustitución de Liskov):
 * guardar() inserta o actualiza; listar() devuelve los pacientes ordenados por nombre.
 */
public interface RepositorioDePacientes {

    void guardar(Paciente paciente);

    Optional<Paciente> buscarPorId(String pacienteId);

    Optional<Paciente> buscarPorDocumento(DocumentoIdentidad documento);

    List<Paciente> listar();
}
