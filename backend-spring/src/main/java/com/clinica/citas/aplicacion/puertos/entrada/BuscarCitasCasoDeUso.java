package com.clinica.citas.aplicacion.puertos.entrada;

import com.clinica.citas.aplicacion.consultas.CitaDetallada;
import com.clinica.citas.aplicacion.consultas.CriteriosDeBusquedaDeCitas;
import java.util.List;

public interface BuscarCitasCasoDeUso {

    List<CitaDetallada> ejecutar(CriteriosDeBusquedaDeCitas criterios);
}
