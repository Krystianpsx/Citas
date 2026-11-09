package com.clinica.citas.infraestructura.persistencia.memoria;

import com.clinica.citas.aplicacion.consultas.CriteriosDeBusquedaDeCitas;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeCitas;
import com.clinica.citas.dominio.modelo.Cita;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class RepositorioDeCitasEnMemoria implements RepositorioDeCitas {

    private final AlmacenEnMemoria<Cita> almacen =
            new AlmacenEnMemoria<>(RepositorioDeCitasEnMemoria::copiar, Comparator.comparing(Cita::inicio));

    @Override
    public void guardar(Cita cita) {
        almacen.guardar(cita);
    }

    @Override
    public Optional<Cita> buscarPorId(String citaId) {
        return almacen.obtener(citaId);
    }

    @Override
    public List<Cita> buscar(CriteriosDeBusquedaDeCitas criterios) {
        return almacen.todasLasQueCumplan(criterios::coincideCon);
    }

    /** Cita es mutable: se guarda una copia, igual que haría una base de datos. */
    private static Cita copiar(Cita cita) {
        return new Cita(cita.id(), cita.pacienteId(), cita.medicoId(), cita.intervalo(), cita.motivo(),
                cita.estado(), cita.motivoCancelacion().orElse(null));
    }
}
