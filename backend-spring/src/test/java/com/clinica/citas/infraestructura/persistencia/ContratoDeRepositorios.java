package com.clinica.citas.infraestructura.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.clinica.citas.aplicacion.consultas.CriteriosDeBusquedaDeCitas;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeCitas;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeMedicos;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDePacientes;
import com.clinica.citas.dominio.modelo.Cita;
import com.clinica.citas.dominio.modelo.DocumentoIdentidad;
import com.clinica.citas.dominio.modelo.Email;
import com.clinica.citas.dominio.modelo.Especialidad;
import com.clinica.citas.dominio.modelo.EstadoCita;
import com.clinica.citas.dominio.modelo.IntervaloDeTiempo;
import com.clinica.citas.dominio.modelo.Medico;
import com.clinica.citas.dominio.modelo.Paciente;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Sustitución de Liskov en la práctica: estas MISMAS pruebas se ejecutan contra cada
 * implementación de los repositorios (en memoria y JPA). Si todas las pasan, cualquiera
 * de ellas puede usarse sin que los casos de uso noten la diferencia.
 */
public abstract class ContratoDeRepositorios {

    protected abstract RepositorioDePacientes pacientes();

    protected abstract RepositorioDeMedicos medicos();

    protected abstract RepositorioDeCitas citas();

    private static Paciente paciente(String nombre, String documento) {
        return Paciente.registrar(nombre, new DocumentoIdentidad(documento), new Email("ana@correo.test"),
                null, null, LocalDate.of(2026, 1, 1));
    }

    private static Medico medico(String nombre, Especialidad especialidad, String registro) {
        return Medico.registrar(nombre, especialidad, registro, new Email("medico@clinica.test"));
    }

    private static Cita cita(Paciente paciente, Medico medico, LocalDateTime inicio) {
        return Cita.programar(paciente.id(), medico.id(),
                IntervaloDeTiempo.desde(inicio, Duration.ofMinutes(30)), "Control");
    }

    private Cita citaGuardada(LocalDateTime inicio) {
        Paciente paciente = paciente("Ana Torres", "1023456789");
        Medico medico = medico("Dra. Lucía", Especialidad.PEDIATRIA, "RM-1001");
        pacientes().guardar(paciente);
        medicos().guardar(medico);
        Cita cita = cita(paciente, medico, inicio);
        citas().guardar(cita);
        return cita;
    }

    @Test
    void guarda_y_recupera_un_paciente() {
        Paciente original = paciente("Ana Torres", "1023456789");
        pacientes().guardar(original);

        Paciente recuperado = pacientes().buscarPorId(original.id()).orElseThrow();

        assertEquals(original, recuperado);
        assertEquals(original.documento(), recuperado.documento());
        assertEquals(Optional.of(original), pacientes().buscarPorDocumento(new DocumentoIdentidad("1023456789")));
        assertTrue(pacientes().buscarPorId("no-existe").isEmpty());
    }

    @Test
    void lista_pacientes_ordenados_por_nombre() {
        pacientes().guardar(paciente("Zoe", "11111111"));
        pacientes().guardar(paciente("Andrés", "22222222"));
        assertEquals(List.of("Andrés", "Zoe"), pacientes().listar().stream().map(Paciente::nombre).toList());
    }

    @Test
    void filtra_medicos_por_especialidad_y_busca_por_registro() {
        medicos().guardar(medico("Dra. A", Especialidad.PEDIATRIA, "RM-1111"));
        medicos().guardar(medico("Dr. B", Especialidad.CARDIOLOGIA, "RM-2222"));

        assertEquals(List.of("Dr. B"),
                medicos().listarPorEspecialidad(Especialidad.CARDIOLOGIA).stream().map(Medico::nombre).toList());
        assertEquals("Dra. A", medicos().buscarPorRegistro("RM-1111").orElseThrow().nombre());
    }

    @Test
    void guardar_dos_veces_actualiza() {
        Cita cita = citaGuardada(LocalDateTime.of(2026, 11, 10, 9, 0));

        cita.confirmar();
        citas().guardar(cita);

        assertEquals(EstadoCita.CONFIRMADA, citas().buscarPorId(cita.id()).orElseThrow().estado());
        assertEquals(1, citas().buscar(CriteriosDeBusquedaDeCitas.todas()).size());
    }

    @Test
    void modificar_lo_recuperado_no_altera_lo_guardado() {
        Cita cita = citaGuardada(LocalDateTime.of(2026, 11, 10, 9, 0));

        citas().buscarPorId(cita.id()).orElseThrow().confirmar(); // sin guardar

        assertEquals(EstadoCita.PROGRAMADA, citas().buscarPorId(cita.id()).orElseThrow().estado());
    }

    @Test
    void busca_citas_por_dia_medico_y_estado_ordenadas_por_hora() {
        Paciente paciente = paciente("Ana Torres", "1023456789");
        Medico medicoA = medico("A", Especialidad.PEDIATRIA, "RM-AAAA");
        Medico medicoB = medico("B", Especialidad.PEDIATRIA, "RM-BBBB");
        pacientes().guardar(paciente);
        medicos().guardar(medicoA);
        medicos().guardar(medicoB);
        Cita tarde = cita(paciente, medicoA, LocalDateTime.of(2026, 11, 10, 15, 0));
        Cita manana = cita(paciente, medicoA, LocalDateTime.of(2026, 11, 10, 8, 0));
        Cita otroDia = cita(paciente, medicoA, LocalDateTime.of(2026, 11, 11, 9, 0));
        Cita otroMedico = cita(paciente, medicoB, LocalDateTime.of(2026, 11, 10, 10, 0));
        List.of(tarde, manana, otroDia, otroMedico).forEach(citas()::guardar);

        var delDia = citas().buscar(CriteriosDeBusquedaDeCitas.todas()
                .delDia(LocalDate.of(2026, 11, 10)).delMedico(medicoA.id()));
        var confirmadas = citas().buscar(CriteriosDeBusquedaDeCitas.todas().conEstado(EstadoCita.CONFIRMADA));

        assertEquals(List.of(manana.id(), tarde.id()), delDia.stream().map(Cita::id).toList());
        assertTrue(confirmadas.isEmpty());
    }
}
