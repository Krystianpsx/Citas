package com.clinica.citas.dominio.modelo;

import com.clinica.citas.dominio.excepciones.DatoInvalido;
import java.util.regex.Pattern;

/** Número de documento: entre 6 y 12 dígitos. Acepta puntos, guiones o espacios al escribirlo. */
public record DocumentoIdentidad(String numero) {

    private static final Pattern SEPARADORES = Pattern.compile("[\\s.\\-]");
    private static final Pattern SOLO_DIGITOS = Pattern.compile("^\\d{6,12}$");

    public DocumentoIdentidad {
        if (numero == null) {
            throw new DatoInvalido("El documento de identidad es obligatorio.");
        }
        String limpio = SEPARADORES.matcher(numero).replaceAll("");
        if (!SOLO_DIGITOS.matcher(limpio).matches()) {
            throw new DatoInvalido("El documento debe tener entre 6 y 12 dígitos.");
        }
        numero = limpio;
    }

    @Override
    public String toString() {
        return numero;
    }
}
