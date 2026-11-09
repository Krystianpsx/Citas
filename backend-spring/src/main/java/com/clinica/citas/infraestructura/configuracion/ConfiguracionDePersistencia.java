package com.clinica.citas.infraestructura.configuracion;

import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeCitas;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeMedicos;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDePacientes;
import com.clinica.citas.infraestructura.persistencia.jpa.CitasSpringData;
import com.clinica.citas.infraestructura.persistencia.jpa.MedicosSpringData;
import com.clinica.citas.infraestructura.persistencia.jpa.PacientesSpringData;
import com.clinica.citas.infraestructura.persistencia.jpa.RepositorioDeCitasJpa;
import com.clinica.citas.infraestructura.persistencia.jpa.RepositorioDeMedicosJpa;
import com.clinica.citas.infraestructura.persistencia.jpa.RepositorioDePacientesJpa;
import com.clinica.citas.infraestructura.persistencia.memoria.RepositorioDeCitasEnMemoria;
import com.clinica.citas.infraestructura.persistencia.memoria.RepositorioDeMedicosEnMemoria;
import com.clinica.citas.infraestructura.persistencia.memoria.RepositorioDePacientesEnMemoria;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elige qué adaptador implementa los puertos de persistencia según la propiedad
 * {@code citas.persistencia}: "jpa" (por defecto) o "memoria". Los casos de uso no notan
 * la diferencia (Sustitución de Liskov).
 */
@Configuration(proxyBeanMethods = false)
public class ConfiguracionDePersistencia {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "citas.persistencia", havingValue = "jpa", matchIfMissing = true)
    static class ConBaseDeDatos {

        @Bean
        RepositorioDePacientes repositorioDePacientes(PacientesSpringData datos) {
            return new RepositorioDePacientesJpa(datos);
        }

        @Bean
        RepositorioDeMedicos repositorioDeMedicos(MedicosSpringData datos) {
            return new RepositorioDeMedicosJpa(datos);
        }

        @Bean
        RepositorioDeCitas repositorioDeCitas(CitasSpringData datos) {
            return new RepositorioDeCitasJpa(datos);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "citas.persistencia", havingValue = "memoria")
    static class EnMemoria {

        @Bean
        RepositorioDePacientes repositorioDePacientes() {
            return new RepositorioDePacientesEnMemoria();
        }

        @Bean
        RepositorioDeMedicos repositorioDeMedicos() {
            return new RepositorioDeMedicosEnMemoria();
        }

        @Bean
        RepositorioDeCitas repositorioDeCitas() {
            return new RepositorioDeCitasEnMemoria();
        }
    }
}
