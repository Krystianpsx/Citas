from dataclasses import dataclass
from datetime import date

from app.dominio.entidad import Entidad
from app.dominio.excepciones import DatoInvalido
from app.dominio.objetos_valor import DocumentoIdentidad, Email
from app.dominio.validaciones import texto_opcional, texto_requerido

LONGITUD_MAXIMA_NOMBRE = 120
LONGITUD_MAXIMA_TELEFONO = 20


@dataclass(eq=False, kw_only=True)
class Paciente(Entidad):
    nombre: str
    documento: DocumentoIdentidad
    email: Email
    telefono: str | None = None
    fecha_nacimiento: date | None = None

    def __post_init__(self) -> None:
        self.nombre = texto_requerido(self.nombre, "nombre", LONGITUD_MAXIMA_NOMBRE)
        self.telefono = texto_opcional(self.telefono, "teléfono", LONGITUD_MAXIMA_TELEFONO)

    @classmethod
    def registrar(
        cls,
        *,
        nombre: str,
        documento: DocumentoIdentidad,
        email: Email,
        hoy: date,
        telefono: str | None = None,
        fecha_nacimiento: date | None = None,
    ) -> "Paciente":
        """Crea un paciente nuevo. La fecha de hoy llega como parámetro para que el
        dominio no consulte el reloj del sistema y sea fácil de probar."""
        if fecha_nacimiento is not None and fecha_nacimiento > hoy:
            raise DatoInvalido("La fecha de nacimiento no puede estar en el futuro.")
        return cls(
            nombre=nombre,
            documento=documento,
            email=email,
            telefono=telefono,
            fecha_nacimiento=fecha_nacimiento,
        )
