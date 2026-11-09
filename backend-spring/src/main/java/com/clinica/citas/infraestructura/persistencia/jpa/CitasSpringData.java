package com.clinica.citas.infraestructura.persistencia.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Las búsquedas con filtros opcionales se arman con Specification (ver RepositorioDeCitasJpa). */
public interface CitasSpringData extends JpaRepository<CitaJpa, String>, JpaSpecificationExecutor<CitaJpa> {
}
