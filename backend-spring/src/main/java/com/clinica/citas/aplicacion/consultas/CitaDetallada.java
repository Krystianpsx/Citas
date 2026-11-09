package com.clinica.citas.aplicacion.consultas;

import com.clinica.citas.dominio.modelo.Cita;
import com.clinica.citas.dominio.modelo.Medico;
import com.clinica.citas.dominio.modelo.Paciente;

/**
 * Modelo de lectura: la cita junto con las personas involucradas. Evita que la
 * interfaz tenga que pedir paciente y médico por separado.
 */
public record CitaDetallada(Cita cita, Paciente paciente, Medico medico) {
}
