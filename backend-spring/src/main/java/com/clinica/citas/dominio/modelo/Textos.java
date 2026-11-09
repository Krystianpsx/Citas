package com.clinica.citas.dominio.modelo;

import com.clinica.citas.dominio.excepciones.DatoInvalido;

/**
 * Validaciones de texto compartidas por las entidades.
 *
 * <p>Refactorización <em>Extract Function</em>: antes cada entidad repetía el mismo
 * bloque para limpiar espacios, exigir el valor y controlar la longitud.
 */
public final class Textos {

    private Textos() {
    }

    public static String requerido(String valor, String campo, int longitudMaxima) {
        if (valor == null || valor.isBlank()) {
            throw new DatoInvalido("El campo '" + campo + "' es obligatorio.");
        }
        return validarLongitud(normalizarEspacios(valor), campo, longitudMaxima);
    }

    /** Igual que {@link #requerido}, pero devuelve null si el valor no viene o está vacío. */
    public static String opcional(String valor, String campo, int longitudMaxima) {
        return valor == null || valor.isBlank() ? null : requerido(valor, campo, longitudMaxima);
    }

    private static String normalizarEspacios(String valor) {
        return valor.strip().replaceAll("\\s+", " ");
    }

    private static String validarLongitud(String valor, String campo, int longitudMaxima) {
        if (valor.length() > longitudMaxima) {
            throw new DatoInvalido("El campo '" + campo + "' admite como máximo " + longitudMaxima + " caracteres.");
        }
        return valor;
    }
}
