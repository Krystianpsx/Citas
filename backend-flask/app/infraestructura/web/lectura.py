"""Lectura segura de datos de la petición."""
from collections.abc import Callable, Mapping
from datetime import date, datetime
from typing import Any, TypeVar

from flask import request

T = TypeVar("T")


class SolicitudInvalida(Exception):
    """El cliente envió datos que no se pueden interpretar (JSON mal formado,
    campos ausentes o fechas ilegibles). Se responde con 400."""

    def __init__(self, mensaje: str) -> None:
        super().__init__(mensaje)
        self.mensaje = mensaje


class DatosDeEntrada:
    """Envuelve un diccionario (cuerpo JSON o parámetros de la URL) y ofrece
    lecturas con nombre: el controlador queda corto y sin if repetidos."""

    def __init__(self, datos: Mapping[str, Any]) -> None:
        self._datos = datos

    @classmethod
    def del_cuerpo(cls) -> "DatosDeEntrada":
        cuerpo = request.get_json(silent=True)
        if not isinstance(cuerpo, dict):
            raise SolicitudInvalida("El cuerpo de la petición debe ser un objeto JSON.")
        return cls(cuerpo)

    @classmethod
    def de_la_url(cls) -> "DatosDeEntrada":
        return cls(request.args)

    def texto(self, campo: str) -> str:
        valor = self._datos.get(campo)
        if not isinstance(valor, str):
            raise SolicitudInvalida(f"Falta el campo '{campo}' o no es un texto.")
        return valor

    def texto_opcional(self, campo: str) -> str | None:
        valor = self._datos.get(campo)
        return valor if isinstance(valor, str) and valor.strip() else None

    def fecha(self, campo: str) -> date:
        return _interpretar(self.texto(campo), date.fromisoformat, campo, "AAAA-MM-DD")

    def fecha_opcional(self, campo: str) -> date | None:
        valor = self.texto_opcional(campo)
        return _interpretar(valor, date.fromisoformat, campo, "AAAA-MM-DD") if valor else None

    def fecha_y_hora(self, campo: str) -> datetime:
        return _interpretar(self.texto(campo), datetime.fromisoformat, campo, "AAAA-MM-DDTHH:MM")


def _interpretar(valor: str, convertir: Callable[[str], T], campo: str, formato: str) -> T:
    try:
        return convertir(valor)
    except ValueError:
        raise SolicitudInvalida(f"El campo '{campo}' debe tener el formato {formato}.") from None
