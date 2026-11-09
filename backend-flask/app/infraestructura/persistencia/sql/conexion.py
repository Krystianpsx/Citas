from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker
from sqlalchemy.pool import StaticPool

from app.infraestructura.persistencia.sql.modelos import Base


def crear_fabrica_de_sesiones(url_base_de_datos: str) -> sessionmaker[Session]:
    """Crea el motor, las tablas si no existen y una fábrica de sesiones."""
    motor = create_engine(url_base_de_datos, **_opciones_del_motor(url_base_de_datos))
    Base.metadata.create_all(motor)
    return sessionmaker(bind=motor, expire_on_commit=False)


def _opciones_del_motor(url: str) -> dict:
    if not url.startswith("sqlite"):
        return {}
    opciones: dict = {"connect_args": {"check_same_thread": False}}
    if ":memory:" in url or url in ("sqlite://", "sqlite:///"):
        opciones["poolclass"] = StaticPool  # una sola conexión para que la base en memoria no se pierda
    return opciones
