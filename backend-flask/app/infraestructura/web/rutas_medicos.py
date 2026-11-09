from flask import Blueprint, jsonify

from app.aplicacion.comandos import ConsultarDisponibilidadComando, RegistrarMedicoComando
from app.aplicacion.puertos.entrada import (
    ConsultarDisponibilidadCasoDeUso,
    ListarMedicosCasoDeUso,
    RegistrarMedicoCasoDeUso,
)
from app.dominio.medico import Especialidad
from app.infraestructura.web.lectura import DatosDeEntrada
from app.infraestructura.web.serializadores import especialidad_a_json, fecha_y_hora_a_texto, medico_a_json


def crear_rutas_de_medicos(
    *,
    registrar: RegistrarMedicoCasoDeUso,
    listar: ListarMedicosCasoDeUso,
    consultar_disponibilidad: ConsultarDisponibilidadCasoDeUso,
) -> Blueprint:
    rutas = Blueprint("medicos", __name__, url_prefix="/api/medicos")

    @rutas.get("")
    def listar_medicos():
        especialidad = DatosDeEntrada.de_la_url().texto_opcional("especialidad")
        return jsonify([medico_a_json(medico) for medico in listar.ejecutar(especialidad)])

    @rutas.post("")
    def registrar_medico():
        datos = DatosDeEntrada.del_cuerpo()
        comando = RegistrarMedicoComando(
            nombre=datos.texto("nombre"),
            especialidad=datos.texto("especialidad"),
            registro_profesional=datos.texto("registroProfesional"),
            email=datos.texto("email"),
        )
        return jsonify(medico_a_json(registrar.ejecutar(comando))), 201

    @rutas.get("/especialidades")
    def listar_especialidades():
        return jsonify([especialidad_a_json(especialidad) for especialidad in Especialidad])

    @rutas.get("/<medico_id>/disponibilidad")
    def consultar_disponibilidad_del_medico(medico_id: str):
        dia = DatosDeEntrada.de_la_url().fecha("fecha")
        horas_libres = consultar_disponibilidad.ejecutar(ConsultarDisponibilidadComando(medico_id=medico_id, dia=dia))
        return jsonify([fecha_y_hora_a_texto(hora) for hora in horas_libres])

    return rutas
