package com.clinica.citas.aplicacion.puertos.entrada;

import com.clinica.citas.dominio.modelo.Paciente;
import java.util.List;

public interface ListarPacientesCasoDeUso {

    List<Paciente> ejecutar();
}
