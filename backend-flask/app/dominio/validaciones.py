"""Validaciones reutilizables por las entidades.

Refactorización aplicada: *Extract Function*. Antes, cada entidad repetía el
mismo bloque para limpiar y validar textos; ahora existe un único lugar.
"""
from app.dominio.excepciones import DatoInvalido


def texto_requerido(valor: object, campo: str, longitud_maxima: int) -> str:
    """Devuelve el texto sin espacios sobrantes o lanza DatoInvalido."""
    if not isinstance(valor, str) or not valor.strip():
        raise DatoInvalido(f"El campo '{campo}' es obligatorio.")
    return _validar_longitud(_normalizar_espacios(valor), campo, longitud_maxima)


def texto_opcional(valor: object, campo: str, longitud_maxima: int) -> str | None:
    """Igual que texto_requerido, pero acepta None o cadenas vacías."""
    if valor is None or (isinstance(valor, str) and not valor.strip()):
        return None
    return texto_requerido(valor, campo, longitud_maxima)


def _normalizar_espacios(valor: str) -> str:
    return " ".join(valor.split())


def _validar_longitud(valor: str, campo: str, longitud_maxima: int) -> str:
    if len(valor) > longitud_maxima:
        raise DatoInvalido(f"El campo '{campo}' admite como máximo {longitud_maxima} caracteres.")
    return valor
