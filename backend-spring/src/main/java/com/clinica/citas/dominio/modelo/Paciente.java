package com.clinica.citas.dominio.modelo;

import com.clinica.citas.dominio.excepciones.DatoInvalido;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public final class Paciente extends Entidad {

    public static final int LONGITUD_MAXIMA_NOMBRE = 120;
    public static final int LONGITUD_MAXIMA_TELEFONO = 20;

    private final String nombre;
    private final DocumentoIdentidad documento;
    private final Email email;
    private final String telefono;
    private final LocalDate fechaNacimiento;

    /** Reconstruye un paciente existente (por ejemplo, al leerlo de la base de datos). */
    public Paciente(String id, String nombre, DocumentoIdentidad documento, Email email,
                    String telefono, LocalDate fechaNacimiento) {
        super(id);
        this.nombre = Textos.requerido(nombre, "nombre", LONGITUD_MAXIMA_NOMBRE);
        this.documento = Objects.requireNonNull(documento, "El documento es obligatorio");
        this.email = Objects.requireNonNull(email, "El correo es obligatorio");
        this.telefono = Textos.opcional(telefono, "teléfono", LONGITUD_MAXIMA_TELEFONO);
        this.fechaNacimiento = fechaNacimiento;
    }

    /**
     * Registra un paciente nuevo. La fecha de hoy llega como parámetro para que el
     * dominio no consulte el reloj del sistema y sea fácil de probar.
     */
    public static Paciente registrar(String nombre, DocumentoIdentidad documento, Email email,
                                     String telefono, LocalDate fechaNacimiento, LocalDate hoy) {
        if (fechaNacimiento != null && fechaNacimiento.isAfter(hoy)) {
            throw new DatoInvalido("La fecha de nacimiento no puede estar en el futuro.");
        }
        return new Paciente(nuevoIdentificador(), nombre, documento, email, telefono, fechaNacimiento);
    }

    public String nombre() {
        return nombre;
    }

    public DocumentoIdentidad documento() {
        return documento;
    }

    public Email email() {
        return email;
    }

    public Optional<String> telefono() {
        return Optional.ofNullable(telefono);
    }

    public Optional<LocalDate> fechaNacimiento() {
        return Optional.ofNullable(fechaNacimiento);
    }
}
