package com.clinica.citas.infraestructura.persistencia.jpa;

import com.clinica.citas.dominio.modelo.Especialidad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicos")
public class MedicoJpa {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Especialidad especialidad;

    @Column(nullable = false, unique = true, length = 20)
    private String registroProfesional;

    @Column(nullable = false, length = 254)
    private String email;

    protected MedicoJpa() {
        // requerido por JPA
    }

    MedicoJpa(String id, String nombre, Especialidad especialidad, String registroProfesional, String email) {
        this.id = id;
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.registroProfesional = registroProfesional;
        this.email = email;
    }

    String getId() {
        return id;
    }

    String getNombre() {
        return nombre;
    }

    Especialidad getEspecialidad() {
        return especialidad;
    }

    String getRegistroProfesional() {
        return registroProfesional;
    }

    String getEmail() {
        return email;
    }
}
