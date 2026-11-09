package com.clinica.citas.aplicacion.servicios;

import com.clinica.citas.aplicacion.consultas.CitaDetallada;
import com.clinica.citas.aplicacion.consultas.CriteriosDeBusquedaDeCitas;
import com.clinica.citas.aplicacion.puertos.entrada.BuscarCitasCasoDeUso;
import com.clinica.citas.aplicacion.puertos.salida.RepositorioDeCitas;
import java.util.List;

public class BuscarCitasServicio implements BuscarCitasCasoDeUso {

    private final RepositorioDeCitas citas;
    private final DetalladorDeCitas detallador;

    public BuscarCitasServicio(RepositorioDeCitas citas, DetalladorDeCitas detallador) {
        this.citas = citas;
        this.detallador = detallador;
    }

    @Override
    public List<CitaDetallada> ejecutar(CriteriosDeBusquedaDeCitas criterios) {
        return detallador.detallarVarias(citas.buscar(criterios));
    }
}
