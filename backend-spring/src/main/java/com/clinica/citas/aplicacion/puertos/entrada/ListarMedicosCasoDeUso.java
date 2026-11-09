package com.clinica.citas.aplicacion.puertos.entrada;

import com.clinica.citas.dominio.modelo.Medico;
import java.util.List;

public interface ListarMedicosCasoDeUso {

    List<Medico> todos();

    List<Medico> deLaEspecialidad(String codigoDeEspecialidad);
}
