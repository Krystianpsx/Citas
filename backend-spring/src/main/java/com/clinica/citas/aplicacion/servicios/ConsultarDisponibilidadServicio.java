package com.clinica.citas.aplicacion.servicios;

import com.clinica.citas.aplicacion.consultas.CriteriosDeBusquedaDeCitas;
import com.clinica.citas.aplicacion.puertos.entrada.ConsultarDisponibilidadCasoDeUso;
import com.clinica.citas.aplicacion.puertos.salida.Reloj;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeCitas;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeMedicos;
import com.clinica.citas.dominio.excepciones.EntidadNoEncontrada;
import com.clinica.citas.dominio.modelo.Cita;
import com.clinica.citas.dominio.modelo.HorarioDeAtencion;
import com.clinica.citas.dominio.modelo.IntervaloDeTiempo;
import java.time.LocalDateTime;
import java.util.List;

public class ConsultarDisponibilidadServicio implements ConsultarDisponibilidadCasoDeUso {

    private final RepositorioDeMedicos medicos;
    private final RepositorioDeCitas citas;
    private final HorarioDeAtencion horario;
    private final Reloj reloj;

    public ConsultarDisponibilidadServicio(RepositorioDeMedicos medicos, RepositorioDeCitas citas,
                                           HorarioDeAtencion horario, Reloj reloj) {
        this.medicos = medicos;
        this.citas = citas;
        this.horario = horario;
        this.reloj = reloj;
    }

    @Override
    public List<LocalDateTime> ejecutar(Comando comando) {
        medicos.buscarPorId(comando.medicoId())
                .orElseThrow(() -> EntidadNoEncontrada.conId("el médico", comando.medicoId()));
        List<Cita> agenda = citas.buscar(
                CriteriosDeBusquedaDeCitas.todas().delDia(comando.dia()).delMedico(comando.medicoId()));
        LocalDateTime ahora = reloj.ahora();
        return horario.franjasDelDia(comando.dia()).stream()
                .filter(franja -> franja.inicio().isAfter(ahora))
                .filter(franja -> estaLibre(franja, agenda))
                .map(IntervaloDeTiempo::inicio)
                .toList();
    }

    private static boolean estaLibre(IntervaloDeTiempo franja, List<Cita> agenda) {
        return agenda.stream().noneMatch(cita -> cita.ocupaAgenda() && franja.seSolapaCon(cita.intervalo()));
    }
}
