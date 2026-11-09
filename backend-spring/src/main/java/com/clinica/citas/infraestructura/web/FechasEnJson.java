package com.clinica.citas.infraestructura.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Formato de fechas en las respuestas: "AAAA-MM-DD" y "AAAA-MM-DDTHH:MM", sin segundos,
 * igual que la versión Flask, para que el mismo frontend funcione con ambos backends.
 */
public final class FechasEnJson {

    private static final DateTimeFormatter FECHA_Y_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private FechasEnJson() {
    }

    public static String texto(LocalDateTime momento) {
        return FECHA_Y_HORA.format(momento);
    }

    public static String texto(LocalDate dia) {
        return dia == null ? null : dia.toString();
    }
}
