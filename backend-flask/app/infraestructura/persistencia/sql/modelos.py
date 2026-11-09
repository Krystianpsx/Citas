"""Tablas de la base de datos. Son modelos de persistencia, no de dominio:
las entidades del dominio nunca heredan de SQLAlchemy."""
from datetime import date, datetime

from sqlalchemy import Date, DateTime, ForeignKey, String
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


class Base(DeclarativeBase):
    pass


class PacienteFila(Base):
    __tablename__ = "pacientes"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    nombre: Mapped[str] = mapped_column(String(120))
    documento: Mapped[str] = mapped_column(String(12), unique=True, index=True)
    email: Mapped[str] = mapped_column(String(254))
    telefono: Mapped[str | None] = mapped_column(String(20))
    fecha_nacimiento: Mapped[date | None] = mapped_column(Date)


class MedicoFila(Base):
    __tablename__ = "medicos"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    nombre: Mapped[str] = mapped_column(String(120))
    especialidad: Mapped[str] = mapped_column(String(30), index=True)
    registro_profesional: Mapped[str] = mapped_column(String(20), unique=True)
    email: Mapped[str] = mapped_column(String(254))


class CitaFila(Base):
    __tablename__ = "citas"

    id: Mapped[str] = mapped_column(String(36), primary_key=True)
    paciente_id: Mapped[str] = mapped_column(ForeignKey("pacientes.id"), index=True)
    medico_id: Mapped[str] = mapped_column(ForeignKey("medicos.id"), index=True)
    inicio: Mapped[datetime] = mapped_column(DateTime, index=True)
    fin: Mapped[datetime] = mapped_column(DateTime)
    motivo: Mapped[str] = mapped_column(String(250))
    estado: Mapped[str] = mapped_column(String(20))
    motivo_cancelacion: Mapped[str | None] = mapped_column(String(250))
