package com.clinica.citas.infraestructura.persistencia.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/** Tabla de pacientes. Es un modelo de persistencia: la entidad del dominio no conoce JPA. */
@Entity
@Table(name = "pacientes")
public class PacienteJpa {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 12)
    private String documento;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(length = 20)
    private String telefono;

    private LocalDate fechaNacimiento;

    protected PacienteJpa() {
        // requerido por JPA
    }

    PacienteJpa(String id, String nombre, String documento, String email, String telefono, LocalDate fechaNacimiento) {
        this.id = id;
        this.nombre = nombre;
        this.documento = documento;
        this.email = email;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
    }

    String getId() {
        return id;
    }

    String getNombre() {
        return nombre;
    }

    String getDocumento() {
        return documento;
    }

    String getEmail() {
        return email;
    }

    String getTelefono() {
        return telefono;
    }

    LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }
}
