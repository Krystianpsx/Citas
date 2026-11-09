"""Clase base para entidades: objetos con identidad propia que perdura en el tiempo."""
from dataclasses import dataclass, field
from uuid import uuid4


def generar_identificador() -> str:
    return str(uuid4())


@dataclass(eq=False, kw_only=True)
class Entidad:
    """Dos entidades son la misma si comparten identificador, aunque cambien sus datos.

    El identificador se genera en el dominio (UUID) y no en la base de datos,
    para que el núcleo no dependa de cómo se persiste.
    """

    id: str = field(default_factory=generar_identificador)

    def __eq__(self, otra: object) -> bool:
        return isinstance(otra, type(self)) and otra.id == self.id

    def __hash__(self) -> int:
        return hash(self.id)
