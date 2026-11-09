package com.clinica.citas.infraestructura.persistencia;

import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeCitas;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeMedicos;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDePacientes;
import com.clinica.citas.infraestructura.persistencia.jpa.CitasSpringData;
import com.clinica.citas.infraestructura.persistencia.jpa.MedicosSpringData;
import com.clinica.citas.infraestructura.persistencia.jpa.PacientesSpringData;
import com.clinica.citas.infraestructura.persistencia.jpa.RepositorioDeCitasJpa;
import com.clinica.citas.infraestructura.persistencia.jpa.RepositorioDeMedicosJpa;
import com.clinica.citas.infraestructura.persistencia.jpa.RepositorioDePacientesJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * El mismo contrato de repositorios, ahora contra JPA y una base H2 en memoria.
 * {@code @DataJpaTest} levanta solo la capa de persistencia y deshace cada prueba al terminar.
 */
@DataJpaTest
class RepositoriosJpaTest extends ContratoDeRepositorios {

    @Autowired
    private PacientesSpringData pacientesSpringData;

    @Autowired
    private MedicosSpringData medicosSpringData;

    @Autowired
    private CitasSpringData citasSpringData;

    @Override
    protected RepositorioDePacientes pacientes() {
        return new RepositorioDePacientesJpa(pacientesSpringData);
    }

    @Override
    protected RepositorioDeMedicos medicos() {
        return new RepositorioDeMedicosJpa(medicosSpringData);
    }

    @Override
    protected RepositorioDeCitas citas() {
        return new RepositorioDeCitasJpa(citasSpringData);
    }
}
