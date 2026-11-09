"""Objetos de valor: inmutables, se comparan por su contenido y se validan al nacer.

Un objeto de valor que existe es, por definición, un valor válido. Así el resto
del código no necesita volver a preguntar si un correo o un documento son correctos.
"""
import re
from dataclasses import dataclass
from datetime import datetime, timedelta

from app.dominio.excepciones import DatoInvalido

_PATRON_EMAIL = re.compile(r"^[^@\s]+@[^@\s]+\.[a-zA-Z]{2,}$")
_PATRON_DOCUMENTO = re.compile(r"^\d{6,12}$")
_SEPARADORES_DE_DOCUMENTO = re.compile(r"[\s.\-]")


def _exigir_texto(valor: object, descripcion: str) -> str:
    if not isinstance(valor, str):
        raise DatoInvalido(f"{descripcion} es obligatorio.")
    return valor


@dataclass(frozen=True)
class Email:
    valor: str

    def __post_init__(self) -> None:
        normalizado = _exigir_texto(self.valor, "El correo electrónico").strip().lower()
        if not _PATRON_EMAIL.match(normalizado):
            raise DatoInvalido(f"El correo '{self.valor}' no tiene un formato válido.")
        object.__setattr__(self, "valor", normalizado)

    def __str__(self) -> str:
        return self.valor


@dataclass(frozen=True)
class DocumentoIdentidad:
    """Número de documento: solo dígitos, entre 6 y 12. Acepta puntos o guiones al escribirlo."""

    numero: str

    def __post_init__(self) -> None:
        texto = _exigir_texto(self.numero, "El documento de identidad")
        limpio = _SEPARADORES_DE_DOCUMENTO.sub("", texto)
        if not _PATRON_DOCUMENTO.match(limpio):
            raise DatoInvalido("El documento debe tener entre 6 y 12 dígitos.")
        object.__setattr__(self, "numero", limpio)

    def __str__(self) -> str:
        return self.numero


@dataclass(frozen=True)
class IntervaloDeTiempo:
    """Rango [inicio, fin). Dos intervalos contiguos (uno termina cuando el otro empieza) no se solapan."""

    inicio: datetime
    fin: datetime

    def __post_init__(self) -> None:
        if self.fin <= self.inicio:
            raise DatoInvalido("El fin del intervalo debe ser posterior a su inicio.")

    @classmethod
    def desde(cls, inicio: datetime, duracion: timedelta) -> "IntervaloDeTiempo":
        return cls(inicio=inicio, fin=inicio + duracion)

    def se_solapa_con(self, otro: "IntervaloDeTiempo") -> bool:
        return self.inicio < otro.fin and otro.inicio < self.fin
