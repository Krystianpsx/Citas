package com.clinica.citas.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.clinica.citas.aplicacion.puertos.entrada.RegistrarMedicoCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.RegistrarPacienteCasoDeUso;
import com.clinica.citas.dominio.excepciones.DatoInvalido;
import com.clinica.citas.dominio.excepciones.EntidadDuplicada;
import com.clinica.citas.dominio.modelo.Medico;
import com.clinica.citas.dominio.modelo.Paciente;
import com.clinica.citas.infraestructura.configuracion.CasosDeUso;
import com.clinica.citas.soporte.EscenarioDePrueba;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CasosDeUsoDePersonasTest {

    private EscenarioDePrueba escenario;
    private CasosDeUso casos;

    @BeforeEach
    void prepararEscenario() {
        escenario = new EscenarioDePrueba();
        casos = escenario.casos();
    }

    @Test
    void registrar_paciente_normaliza_los_datos() {
        Paciente paciente = casos.registrarPaciente().ejecutar(new RegistrarPacienteCasoDeUso.Comando(
                "  Ana   Torres ", "1.023.456.789", "ANA@correo.test", null, null));

        assertEquals("Ana Torres", paciente.nombre());
        assertEquals("1023456789", paciente.documento().numero());
        assertEquals("ana@correo.test", paciente.email().valor());
    }

    @Test
    void no_permite_documentos_repetidos() {
        escenario.pacienteAna();
        var otra = new RegistrarPacienteCasoDeUso.Comando("Otra", "1023456789", "otra@correo.test", null, null);
        assertThrows(EntidadDuplicada.class, () -> casos.registrarPaciente().ejecutar(otra));
    }

    @Test
    void no_acepta_fecha_de_nacimiento_futura() {
        var bebe = new RegistrarPacienteCasoDeUso.Comando(
                "Bebé", "12345678", "bebe@correo.test", null, LocalDate.of(2030, 1, 1));
        assertThrows(DatoInvalido.class, () -> casos.registrarPaciente().ejecutar(bebe));
    }

    @Test
    void lista_pacientes_por_orden_alfabetico() {
        escenario.pacienteJorge();
        escenario.pacienteAna();
        assertEquals(
                java.util.List.of("Ana Torres", "Jorge Ramírez"),
                casos.listarPacientes().ejecutar().stream().map(Paciente::nombre).toList());
    }

    @Test
    void no_permite_registros_profesionales_repetidos() {
        escenario.doctoraLucia();
        var otro = new RegistrarMedicoCasoDeUso.Comando("Otro", "PEDIATRIA", "rm-1001", "otro@clinica.test");
        assertThrows(EntidadDuplicada.class, () -> casos.registrarMedico().ejecutar(otro));
    }

    @Test
    void especialidad_inexistente() {
        var comando = new RegistrarMedicoCasoDeUso.Comando("X", "ASTROLOGIA", "RM-9999", "x@clinica.test");
        var error = assertThrows(DatoInvalido.class, () -> casos.registrarMedico().ejecutar(comando));
        assertTrue(error.getMessage().contains("ASTROLOGIA"));
    }

    @Test
    void filtrar_medicos_por_especialidad() {
        escenario.doctoraLucia();
        escenario.doctorAndres();
        assertEquals(
                java.util.List.of("Dr. Andrés Salazar"),
                casos.listarMedicos().deLaEspecialidad("CARDIOLOGIA").stream().map(Medico::nombre).toList());
    }
}
