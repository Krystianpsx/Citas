package com.clinica.citas.infraestructura.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

/**
 * Lectura de campos que llegan como texto (cuerpo JSON o parámetros de la URL).
 * Si falta un campo o una fecha no se puede leer, lanza SolicitudInvalida con un
 * mensaje que dice exactamente qué corregir.
 */
public final class CamposDeEntrada {

    private CamposDeEntrada() {
    }

    public static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new SolicitudInvalida("Falta el campo '" + campo + "'.");
        }
        return valor;
    }

    public static LocalDate fecha(String valor, String campo) {
        return interpretar(requerido(valor, campo), LocalDate::parse, campo, "AAAA-MM-DD");
    }

    public static LocalDate fechaOpcional(String valor, String campo) {
        return valor == null || valor.isBlank() ? null : fecha(valor, campo);
    }

    public static LocalDateTime fechaYHora(String valor, String campo) {
        return interpretar(requerido(valor, campo), LocalDateTime::parse, campo, "AAAA-MM-DDTHH:MM");
    }

    private static <T> T interpretar(String valor, Function<String, T> convertir, String campo, String formato) {
        try {
            return convertir.apply(valor);
        } catch (DateTimeParseException error) {
            throw new SolicitudInvalida("El campo '" + campo + "' debe tener el formato " + formato + ".");
        }
    }
}
