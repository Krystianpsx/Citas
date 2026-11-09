from flask import Blueprint, jsonify

from app.aplicacion.comandos import RegistrarPacienteComando
from app.aplicacion.puertos.entrada import ListarPacientesCasoDeUso, RegistrarPacienteCasoDeUso
from app.infraestructura.web.lectura import DatosDeEntrada
from app.infraestructura.web.serializadores import paciente_a_json


def crear_rutas_de_pacientes(
    *, registrar: RegistrarPacienteCasoDeUso, listar: ListarPacientesCasoDeUso
) -> Blueprint:
    rutas = Blueprint("pacientes", __name__, url_prefix="/api/pacientes")

    @rutas.get("")
    def listar_pacientes():
        return jsonify([paciente_a_json(paciente) for paciente in listar.ejecutar()])

    @rutas.post("")
    def registrar_paciente():
        datos = DatosDeEntrada.del_cuerpo()
        comando = RegistrarPacienteComando(
            nombre=datos.texto("nombre"),
            documento=datos.texto("documento"),
            email=datos.texto("email"),
            telefono=datos.texto_opcional("telefono"),
            fecha_nacimiento=datos.fecha_opcional("fechaNacimiento"),
        )
        return jsonify(paciente_a_json(registrar.ejecutar(comando))), 201

    return rutas
