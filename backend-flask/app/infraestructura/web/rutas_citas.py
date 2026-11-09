from flask import Blueprint, jsonify

from app.aplicacion.comandos import AgendarCitaComando, CancelarCitaComando, ReprogramarCitaComando
from app.aplicacion.criterios import CriteriosDeBusquedaDeCitas
from app.aplicacion.puertos.entrada import (
    AgendarCitaCasoDeUso,
    AtenderCitaCasoDeUso,
    BuscarCitasCasoDeUso,
    CancelarCitaCasoDeUso,
    ConfirmarCitaCasoDeUso,
    ReprogramarCitaCasoDeUso,
)
from app.dominio.estado_cita import EstadoCita
from app.infraestructura.web.lectura import DatosDeEntrada, SolicitudInvalida
from app.infraestructura.web.serializadores import cita_a_json


def crear_rutas_de_citas(
    *,
    agendar: AgendarCitaCasoDeUso,
    buscar: BuscarCitasCasoDeUso,
    confirmar: ConfirmarCitaCasoDeUso,
    atender: AtenderCitaCasoDeUso,
    cancelar: CancelarCitaCasoDeUso,
    reprogramar: ReprogramarCitaCasoDeUso,
) -> Blueprint:
    rutas = Blueprint("citas", __name__, url_prefix="/api/citas")

    @rutas.get("")
    def buscar_citas():
        resultado = buscar.ejecutar(_criterios_desde_la_url())
        return jsonify([cita_a_json(detalle) for detalle in resultado])

    @rutas.post("")
    def agendar_cita():
        datos = DatosDeEntrada.del_cuerpo()
        comando = AgendarCitaComando(
            paciente_id=datos.texto("pacienteId"),
            medico_id=datos.texto("medicoId"),
            inicio=datos.fecha_y_hora("inicio"),
            motivo=datos.texto("motivo"),
        )
        return jsonify(cita_a_json(agendar.ejecutar(comando))), 201

    @rutas.post("/<cita_id>/confirmar")
    def confirmar_cita(cita_id: str):
        return jsonify(cita_a_json(confirmar.ejecutar(cita_id)))

    @rutas.post("/<cita_id>/atender")
    def atender_cita(cita_id: str):
        return jsonify(cita_a_json(atender.ejecutar(cita_id)))

    @rutas.post("/<cita_id>/cancelar")
    def cancelar_cita(cita_id: str):
        motivo = DatosDeEntrada.del_cuerpo().texto("motivo")
        return jsonify(cita_a_json(cancelar.ejecutar(CancelarCitaComando(cita_id=cita_id, motivo=motivo))))

    @rutas.post("/<cita_id>/reprogramar")
    def reprogramar_cita(cita_id: str):
        nuevo_inicio = DatosDeEntrada.del_cuerpo().fecha_y_hora("inicio")
        comando = ReprogramarCitaComando(cita_id=cita_id, nuevo_inicio=nuevo_inicio)
        return jsonify(cita_a_json(reprogramar.ejecutar(comando)))

    return rutas


def _criterios_desde_la_url() -> CriteriosDeBusquedaDeCitas:
    datos = DatosDeEntrada.de_la_url()
    return CriteriosDeBusquedaDeCitas(
        dia=datos.fecha_opcional("fecha"),
        medico_id=datos.texto_opcional("medicoId"),
        paciente_id=datos.texto_opcional("pacienteId"),
        estado=_estado_opcional(datos.texto_opcional("estado")),
    )


def _estado_opcional(valor: str | None) -> EstadoCita | None:
    if valor is None:
        return None
    try:
        return EstadoCita(valor)
    except ValueError:
        raise SolicitudInvalida(f"El estado '{valor}' no existe.") from None
