package com.clinica.citas.infraestructura.persistencia;

import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeCitas;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeMedicos;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDePacientes;
import com.clinica.citas.infraestructura.persistencia.memoria.RepositorioDeCitasEnMemoria;
import com.clinica.citas.infraestructura.persistencia.memoria.RepositorioDeMedicosEnMemoria;
import com.clinica.citas.infraestructura.persistencia.memoria.RepositorioDePacientesEnMemoria;

/** El contrato de repositorios aplicado a la implementación en memoria. */
class RepositoriosEnMemoriaTest extends ContratoDeRepositorios {

    private final RepositorioDePacientes pacientes = new RepositorioDePacientesEnMemoria();
    private final RepositorioDeMedicos medicos = new RepositorioDeMedicosEnMemoria();
    private final RepositorioDeCitas citas = new RepositorioDeCitasEnMemoria();

    @Override
    protected RepositorioDePacientes pacientes() {
        return pacientes;
    }

    @Override
    protected RepositorioDeMedicos medicos() {
        return medicos;
    }

    @Override
    protected RepositorioDeCitas citas() {
        return citas;
    }
}
