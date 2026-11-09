import os
from dataclasses import dataclass

PERSISTENCIA_SQL = "sql"
PERSISTENCIA_EN_MEMORIA = "memoria"


@dataclass(frozen=True)
class Ajustes:
    url_base_de_datos: str = "sqlite:///citas_medicas.db"
    persistencia: str = PERSISTENCIA_SQL
    origenes_permitidos: tuple[str, ...] = ("http://localhost:5173",)

    @classmethod
    def desde_entorno(cls) -> "Ajustes":
        return cls(
            url_base_de_datos=os.getenv("DATABASE_URL", cls.url_base_de_datos),
            persistencia=os.getenv("PERSISTENCIA", cls.persistencia),
            origenes_permitidos=_lista(os.getenv("ORIGENES_PERMITIDOS")) or cls.origenes_permitidos,
        )


def _lista(valor: str | None) -> tuple[str, ...]:
    return tuple(origen.strip() for origen in (valor or "").split(",") if origen.strip())
