package com.clinica.citas.dominio.modelo;

import com.clinica.citas.dominio.excepciones.DatoInvalido;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public final class Medico extends Entidad {

    public static final int LONGITUD_MAXIMA_NOMBRE = 120;
    private static final Pattern PATRON_REGISTRO = Pattern.compile("^[A-Z0-9-]{4,20}$");

    private final String nombre;
    private final Especialidad especialidad;
    private final String registroProfesional;
    private final Email email;

    public Medico(String id, String nombre, Especialidad especialidad, String registroProfesional, Email email) {
        super(id);
        this.nombre = Textos.requerido(nombre, "nombre", LONGITUD_MAXIMA_NOMBRE);
        this.especialidad = Objects.requireNonNull(especialidad, "La especialidad es obligatoria");
        this.registroProfesional = validarRegistro(registroProfesional);
        this.email = Objects.requireNonNull(email, "El correo es obligatorio");
    }

    public static Medico registrar(String nombre, Especialidad especialidad, String registroProfesional, Email email) {
        return new Medico(nuevoIdentificador(), nombre, especialidad, registroProfesional, email);
    }

    private static String validarRegistro(String registro) {
        String normalizado = Textos.requerido(registro, "registro profesional", 20).toUpperCase(Locale.ROOT);
        if (!PATRON_REGISTRO.matcher(normalizado).matches()) {
            throw new DatoInvalido("El registro profesional debe tener entre 4 y 20 letras, números o guiones.");
        }
        return normalizado;
    }

    public String nombre() {
        return nombre;
    }

    public Especialidad especialidad() {
        return especialidad;
    }

    public String registroProfesional() {
        return registroProfesional;
    }

    public Email email() {
        return email;
    }
}
