package com.clinica.citas.aplicacion.puertos.entrada;

import com.clinica.citas.dominio.modelo.Medico;

public interface RegistrarMedicoCasoDeUso {

    Medico ejecutar(Comando comando);

    record Comando(String nombre, String especialidad, String registroProfesional, String email) {
    }
}
