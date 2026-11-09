package com.clinica.citas.dominio.modelo;

import com.clinica.citas.dominio.excepciones.DatoInvalido;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Objeto de valor. Si existe, es un correo válido y normalizado: el resto del código
 * no necesita volver a validarlo (refactorización <em>Replace Primitive with Object</em>).
 */
public record Email(String valor) {

    private static final Pattern PATRON = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[a-zA-Z]{2,}$");

    public Email {
        if (valor == null) {
            throw new DatoInvalido("El correo electrónico es obligatorio.");
        }
        String normalizado = valor.strip().toLowerCase(Locale.ROOT);
        if (!PATRON.matcher(normalizado).matches()) {
            throw new DatoInvalido("El correo '" + valor + "' no tiene un formato válido.");
        }
        valor = normalizado;
    }

    @Override
    public String toString() {
        return valor;
    }
}
