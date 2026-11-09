package com.clinica.citas.infraestructura.notificaciones;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class FormatoDeFechas {

    private static final DateTimeFormatter FECHA_Y_HORA_LEGIBLES =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy 'a las' HH:mm", Locale.forLanguageTag("es"));

    private FormatoDeFechas() {
    }

    /** Ejemplo: "martes 10 de noviembre de 2026 a las 09:30". */
    static String legible(LocalDateTime momento) {
        return FECHA_Y_HORA_LEGIBLES.format(momento);
    }
}
