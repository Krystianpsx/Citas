package com.clinica.citas.infraestructura.persistencia.jpa;

import com.clinica.citas.aplicacion.puertos.salida.RepositorioDePacientes;
import com.clinica.citas.dominio.modelo.DocumentoIdentidad;
import com.clinica.citas.dominio.modelo.Paciente;
import java.util.List;
import java.util.Optional;

public class RepositorioDePacientesJpa extends AdaptadorJpa<Paciente, PacienteJpa, PacientesSpringData>
        implements RepositorioDePacientes {

    public RepositorioDePacientesJpa(PacientesSpringData datos) {
        super(datos, MapeadorJpa::aFila, MapeadorJpa::aDominio);
    }

    @Override
    public Optional<Paciente> buscarPorId(String pacienteId) {
        return porId(pacienteId);
    }

    @Override
    public Optional<Paciente> buscarPorDocumento(DocumentoIdentidad documento) {
        return aDominio(datos.findByDocumento(documento.numero()));
    }

    @Override
    public List<Paciente> listar() {
        return aDominio(datos.findAllByOrderByNombreAsc());
    }
}
