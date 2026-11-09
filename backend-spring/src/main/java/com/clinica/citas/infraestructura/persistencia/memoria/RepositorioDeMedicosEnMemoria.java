package com.clinica.citas.infraestructura.persistencia.memoria;

import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeMedicos;
import com.clinica.citas.dominio.modelo.Especialidad;
import com.clinica.citas.dominio.modelo.Medico;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class RepositorioDeMedicosEnMemoria implements RepositorioDeMedicos {

    // Medico es inmutable: no hace falta copiarlo.
    private final AlmacenEnMemoria<Medico> almacen = new AlmacenEnMemoria<>(
            UnaryOperator.identity(), Comparator.comparing(medico -> medico.nombre().toLowerCase()));

    @Override
    public void guardar(Medico medico) {
        almacen.guardar(medico);
    }

    @Override
    public Optional<Medico> buscarPorId(String medicoId) {
        return almacen.obtener(medicoId);
    }

    @Override
    public Optional<Medico> buscarPorRegistro(String registroProfesional) {
        return almacen.primeraQueCumpla(medico -> medico.registroProfesional().equals(registroProfesional));
    }

    @Override
    public List<Medico> listar() {
        return almacen.todasLasQueCumplan(medico -> true);
    }

    @Override
    public List<Medico> listarPorEspecialidad(Especialidad especialidad) {
        return almacen.todasLasQueCumplan(medico -> medico.especialidad() == especialidad);
    }
}
