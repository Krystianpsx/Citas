package com.clinica.citas.dominio.modelo;

import com.clinica.citas.dominio.excepciones.DatoInvalido;
import java.util.Arrays;

public enum Especialidad {
    MEDICINA_GENERAL("Medicina general"),
    PEDIATRIA("Pediatría"),
    CARDIOLOGIA("Cardiología"),
    DERMATOLOGIA("Dermatología"),
    GINECOLOGIA("Ginecología"),
    TRAUMATOLOGIA("Traumatología");

    private final String nombreLegible;

    Especialidad(String nombreLegible) {
        this.nombreLegible = nombreLegible;
    }

    public String nombreLegible() {
        return nombreLegible;
    }

    public static Especialidad desdeCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(especialidad -> especialidad.name().equals(codigo))
                .findFirst()
                .orElseThrow(() -> new DatoInvalido("La especialidad '" + codigo + "' no existe."));
    }
}
