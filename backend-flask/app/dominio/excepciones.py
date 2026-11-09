"""Errores del dominio.

Cada excepción nombra una situación del negocio. La capa web las traduce a
códigos HTTP sin que el dominio sepa que HTTP existe (Inversión de Dependencias).
"""


class ErrorDeDominio(Exception):
    """Raíz de todos los errores que nacen en el núcleo de la aplicación."""

    def __init__(self, mensaje: str) -> None:
        super().__init__(mensaje)
        self.mensaje = mensaje


class DatoInvalido(ErrorDeDominio):
    """Un valor no cumple el formato o las restricciones esperadas."""


class ReglaDeNegocioIncumplida(ErrorDeDominio):
    """La operación está bien formada, pero el negocio no la permite."""


class TransicionDeEstadoInvalida(ReglaDeNegocioIncumplida):
    """Se intentó llevar una cita a un estado que no es alcanzable desde el actual."""


class EntidadNoEncontrada(ErrorDeDominio):
    """No existe la entidad solicitada."""


class EntidadDuplicada(ErrorDeDominio):
    """Ya existe una entidad con el mismo identificador natural (documento, registro)."""
