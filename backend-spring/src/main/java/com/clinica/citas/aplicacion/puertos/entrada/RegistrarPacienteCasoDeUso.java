package com.clinica.citas.aplicacion.puertos.entrada;

import com.clinica.citas.dominio.modelo.Paciente;
import java.time.LocalDate;

public interface RegistrarPacienteCasoDeUso {

    Paciente ejecutar(Comando comando);

    /** Datos de entrada en tipos simples; el caso de uso construye los objetos de valor. */
    record Comando(String nombre, String documento, String email, String telefono, LocalDate fechaNacimiento) {
    }
}
