"""Conversión de objetos del dominio a JSON. Es el único lugar que decide
los nombres de los campos que ve el frontend."""
from datetime import date, datetime
from typing import Any

from app.aplicacion.vistas import CitaDetallada
from app.dominio.medico import Especialidad, Medico
from app.dominio.paciente import Paciente


def fecha_y_hora_a_texto(momento: datetime) -> str:
    return momento.isoformat(timespec="minutes")


def _fecha_a_texto(dia: date | None) -> str | None:
    return dia.isoformat() if dia else None


def paciente_a_json(paciente: Paciente) -> dict[str, Any]:
    return {
        "id": paciente.id,
        "nombre": paciente.nombre,
        "documento": paciente.documento.numero,
        "email": paciente.email.valor,
        "telefono": paciente.telefono,
        "fechaNacimiento": _fecha_a_texto(paciente.fecha_nacimiento),
    }


def especialidad_a_json(especialidad: Especialidad) -> dict[str, str]:
    return {"codigo": especialidad.value, "nombre": especialidad.nombre_legible}


def medico_a_json(medico: Medico) -> dict[str, Any]:
    return {
        "id": medico.id,
        "nombre": medico.nombre,
        "especialidad": especialidad_a_json(medico.especialidad),
        "registroProfesional": medico.registro_profesional,
        "email": medico.email.valor,
    }


def cita_a_json(detalle: CitaDetallada) -> dict[str, Any]:
    cita = detalle.cita
    return {
        "id": cita.id,
        "inicio": fecha_y_hora_a_texto(cita.inicio),
        "fin": fecha_y_hora_a_texto(cita.fin),
        "motivo": cita.motivo,
        "estado": cita.estado.value,
        "motivoCancelacion": cita.motivo_cancelacion,
        "paciente": {"id": detalle.paciente.id, "nombre": detalle.paciente.nombre},
        "medico": {
            "id": detalle.medico.id,
            "nombre": detalle.medico.nombre,
            "especialidad": especialidad_a_json(detalle.medico.especialidad),
        },
    }
