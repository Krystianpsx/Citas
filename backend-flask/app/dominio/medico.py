import re
from dataclasses import dataclass
from enum import Enum

from app.dominio.entidad import Entidad
from app.dominio.excepciones import DatoInvalido
from app.dominio.objetos_valor import Email
from app.dominio.validaciones import texto_requerido

LONGITUD_MAXIMA_NOMBRE = 120
_PATRON_REGISTRO_PROFESIONAL = re.compile(r"^[A-Z0-9-]{4,20}$")


class Especialidad(str, Enum):
    MEDICINA_GENERAL = "MEDICINA_GENERAL"
    PEDIATRIA = "PEDIATRIA"
    CARDIOLOGIA = "CARDIOLOGIA"
    DERMATOLOGIA = "DERMATOLOGIA"
    GINECOLOGIA = "GINECOLOGIA"
    TRAUMATOLOGIA = "TRAUMATOLOGIA"

    @property
    def nombre_legible(self) -> str:
        return _NOMBRES_LEGIBLES[self]

    @classmethod
    def desde_codigo(cls, codigo: str) -> "Especialidad":
        try:
            return cls(codigo)
        except ValueError:
            raise DatoInvalido(f"La especialidad '{codigo}' no existe.") from None


_NOMBRES_LEGIBLES = {
    Especialidad.MEDICINA_GENERAL: "Medicina general",
    Especialidad.PEDIATRIA: "Pediatría",
    Especialidad.CARDIOLOGIA: "Cardiología",
    Especialidad.DERMATOLOGIA: "Dermatología",
    Especialidad.GINECOLOGIA: "Ginecología",
    Especialidad.TRAUMATOLOGIA: "Traumatología",
}


@dataclass(eq=False, kw_only=True)
class Medico(Entidad):
    nombre: str
    especialidad: Especialidad
    registro_profesional: str
    email: Email

    def __post_init__(self) -> None:
        self.nombre = texto_requerido(self.nombre, "nombre", LONGITUD_MAXIMA_NOMBRE)
        self.registro_profesional = self._validar_registro(self.registro_profesional)

    @staticmethod
    def _validar_registro(registro: str) -> str:
        normalizado = texto_requerido(registro, "registro profesional", 20).upper()
        if not _PATRON_REGISTRO_PROFESIONAL.match(normalizado):
            raise DatoInvalido("El registro profesional debe tener entre 4 y 20 letras, números o guiones.")
        return normalizado
