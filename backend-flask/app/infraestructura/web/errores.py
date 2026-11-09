"""Traducción de errores a respuestas HTTP.

Abierto/Cerrado: para un nuevo tipo de error basta con agregar una entrada a la
tabla; no se edita ningún manejador. Se busca recorriendo la jerarquía de la
excepción, así una subclase hereda el código de su padre."""
import logging

from flask import Flask, jsonify
from werkzeug.exceptions import HTTPException

from app.dominio.excepciones import (
    DatoInvalido,
    EntidadDuplicada,
    EntidadNoEncontrada,
    ErrorDeDominio,
    ReglaDeNegocioIncumplida,
)
from app.infraestructura.web.lectura import SolicitudInvalida

_CODIGOS_HTTP: dict[type[Exception], int] = {
    SolicitudInvalida: 400,
    DatoInvalido: 400,
    EntidadNoEncontrada: 404,
    EntidadDuplicada: 409,
    ReglaDeNegocioIncumplida: 422,
}

_registro = logging.getLogger("citas.web")


def registrar_manejadores_de_errores(aplicacion: Flask) -> None:
    aplicacion.register_error_handler(ErrorDeDominio, _responder_error_conocido)
    aplicacion.register_error_handler(SolicitudInvalida, _responder_error_conocido)
    aplicacion.register_error_handler(HTTPException, _responder_error_http)
    aplicacion.register_error_handler(Exception, _responder_error_inesperado)


def _responder_error_conocido(error: ErrorDeDominio | SolicitudInvalida):
    return _respuesta(error.mensaje, type(error).__name__, _codigo_para(error))


def _responder_error_http(error: HTTPException):
    return _respuesta(error.description or "Error HTTP.", type(error).__name__, error.code or 500)


def _responder_error_inesperado(error: Exception):
    _registro.exception("Error no controlado")
    return _respuesta("Ocurrió un error inesperado en el servidor.", "ErrorInterno", 500)


def _codigo_para(error: Exception) -> int:
    for clase in type(error).__mro__:
        if clase in _CODIGOS_HTTP:
            return _CODIGOS_HTTP[clase]
    return 500


def _respuesta(mensaje: str, tipo: str, codigo: int):
    return jsonify({"mensaje": mensaje, "tipo": tipo}), codigo
