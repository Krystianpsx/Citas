package com.clinica.citas.infraestructura.persistencia.jpa;

import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeMedicos;
import com.clinica.citas.dominio.modelo.Especialidad;
import com.clinica.citas.dominio.modelo.Medico;
import java.util.List;
import java.util.Optional;

public class RepositorioDeMedicosJpa extends AdaptadorJpa<Medico, MedicoJpa, MedicosSpringData>
        implements RepositorioDeMedicos {

    public RepositorioDeMedicosJpa(MedicosSpringData datos) {
        super(datos, MapeadorJpa::aFila, MapeadorJpa::aDominio);
    }

    @Override
    public Optional<Medico> buscarPorId(String medicoId) {
        return porId(medicoId);
    }

    @Override
    public Optional<Medico> buscarPorRegistro(String registroProfesional) {
        return aDominio(datos.findByRegistroProfesional(registroProfesional));
    }

    @Override
    public List<Medico> listar() {
        return aDominio(datos.findAllByOrderByNombreAsc());
    }

    @Override
    public List<Medico> listarPorEspecialidad(Especialidad especialidad) {
        return aDominio(datos.findByEspecialidadOrderByNombreAsc(especialidad));
    }
}
