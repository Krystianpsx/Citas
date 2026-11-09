package com.clinica.citas.aplicacion.servicios;

import com.clinica.citas.aplicacion.puertos.entrada.ListarMedicosCasoDeUso;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeMedicos;
import com.clinica.citas.dominio.modelo.Especialidad;
import com.clinica.citas.dominio.modelo.Medico;
import java.util.List;

public class ListarMedicosServicio implements ListarMedicosCasoDeUso {

    private final RepositorioDeMedicos medicos;

    public ListarMedicosServicio(RepositorioDeMedicos medicos) {
        this.medicos = medicos;
    }

    @Override
    public List<Medico> todos() {
        return medicos.listar();
    }

    @Override
    public List<Medico> deLaEspecialidad(String codigoDeEspecialidad) {
        return medicos.listarPorEspecialidad(Especialidad.desdeCodigo(codigoDeEspecialidad));
    }
}
