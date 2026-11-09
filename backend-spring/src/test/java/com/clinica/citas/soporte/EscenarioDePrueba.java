package com.clinica.citas.soporte;

import com.clinica.citas.aplicacion.consultas.CitaDetallada;
import com.clinica.citas.aplicacion.puertos.entrada.AgendarCitaCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.RegistrarMedicoCasoDeUso;
import com.clinica.citas.aplicacion.puertos.entrada.RegistrarPacienteCasoDeUso;
import com.clinica.citas.dominio.modelo.HorarioDeAtencion;
import com.clinica.citas.dominio.modelo.Medico;
import com.clinica.citas.dominio.modelo.Paciente;
import com.clinica.citas.infraestructura.configuracion.CasosDeUso;
import com.clinica.citas.infraestructura.persistencia.memoria.RepositorioDeCitasEnMemoria;
import com.clinica.citas.infraestructura.persistencia.memoria.RepositorioDeMedicosEnMemoria;
import com.clinica.citas.infraestructura.persistencia.memoria.RepositorioDePacientesEnMemoria;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * El núcleo completo, ensamblado igual que en producción (con {@link CasosDeUso#ensamblar}),
 * pero con repositorios en memoria, un reloj fijo y un notificador espía.
 * No arranca Spring: las pruebas tardan milisegundos.
 */
public final class EscenarioDePrueba {

    /** Lunes 9 de noviembre de 2026, 07:00: la clínica aún no abre. */
    public static final LocalDateTime LUNES_TEMPRANO = LocalDateTime.of(2026, 11, 9, 7, 0);
    public static final LocalDate LUNES = LUNES_TEMPRANO.toLocalDate();

    private final RelojFijo reloj = new RelojFijo(LUNES_TEMPRANO);
    private final NotificadorEspia notificador = new NotificadorEspia();
    private final CasosDeUso casos = CasosDeUso.ensamblar(new CasosDeUso.Dependencias(
            new RepositorioDePacientesEnMemoria(),
            new RepositorioDeMedicosEnMemoria(),
            new RepositorioDeCitasEnMemoria(),
            reloj,
            notificador,
            HorarioDeAtencion.deLunesAViernes()));

    public static LocalDateTime aLas(int hora, int minuto) {
        return LUNES.atTime(hora, minuto);
    }

    public RelojFijo reloj() {
        return reloj;
    }

    public NotificadorEspia notificador() {
        return notificador;
    }

    public CasosDeUso casos() {
        return casos;
    }

    public Medico doctoraLucia() {
        return casos.registrarMedico().ejecutar(new RegistrarMedicoCasoDeUso.Comando(
                "Dra. Lucía Fernández", "MEDICINA_GENERAL", "RM-1001", "lucia@clinica.test"));
    }

    public Medico doctorAndres() {
        return casos.registrarMedico().ejecutar(new RegistrarMedicoCasoDeUso.Comando(
                "Dr. Andrés Salazar", "CARDIOLOGIA", "RM-2002", "andres@clinica.test"));
    }

    public Paciente pacienteAna() {
        return casos.registrarPaciente().ejecutar(new RegistrarPacienteCasoDeUso.Comando(
                "Ana Torres", "1023456789", "ana@correo.test", null, null));
    }

    public Paciente pacienteJorge() {
        return casos.registrarPaciente().ejecutar(new RegistrarPacienteCasoDeUso.Comando(
                "Jorge Ramírez", "80123456", "jorge@correo.test", null, null));
    }

    public CitaDetallada agendar(Paciente paciente, Medico medico, LocalDateTime inicio) {
        return casos.agendarCita().ejecutar(
                new AgendarCitaCasoDeUso.Comando(paciente.id(), medico.id(), inicio, "Control general"));
    }
}
