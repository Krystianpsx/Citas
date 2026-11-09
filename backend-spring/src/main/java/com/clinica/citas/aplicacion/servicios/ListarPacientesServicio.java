package com.clinica.citas.aplicacion.servicios;

import com.clinica.citas.aplicacion.puertos.entrada.ListarPacientesCasoDeUso;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDePacientes;
import com.clinica.citas.dominio.modelo.Paciente;
import java.util.List;

public class ListarPacientesServicio implements ListarPacientesCasoDeUso {

    private final RepositorioDePacientes pacientes;

    public ListarPacientesServicio(RepositorioDePacientes pacientes) {
        this.pacientes = pacientes;
    }

    @Override
    public List<Paciente> ejecutar() {
        return pacientes.listar();
    }
}
