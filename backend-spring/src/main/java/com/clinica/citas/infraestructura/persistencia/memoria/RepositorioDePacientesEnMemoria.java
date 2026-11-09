package com.clinica.citas.infraestructura.persistencia.memoria;

import com.clinica.citas.aplicacion.puertos.salida.RepositorioDePacientes;
import com.clinica.citas.dominio.modelo.DocumentoIdentidad;
import com.clinica.citas.dominio.modelo.Paciente;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class RepositorioDePacientesEnMemoria implements RepositorioDePacientes {

    // Paciente es inmutable: no hace falta copiarlo.
    private final AlmacenEnMemoria<Paciente> almacen = new AlmacenEnMemoria<>(
            UnaryOperator.identity(), Comparator.comparing(paciente -> paciente.nombre().toLowerCase()));

    @Override
    public void guardar(Paciente paciente) {
        almacen.guardar(paciente);
    }

    @Override
    public Optional<Paciente> buscarPorId(String pacienteId) {
        return almacen.obtener(pacienteId);
    }

    @Override
    public Optional<Paciente> buscarPorDocumento(DocumentoIdentidad documento) {
        return almacen.primeraQueCumpla(paciente -> paciente.documento().equals(documento));
    }

    @Override
    public List<Paciente> listar() {
        return almacen.todasLasQueCumplan(paciente -> true);
    }
}
