from flask import Blueprint, jsonify

from app.dominio.horario_atencion import HorarioDeAtencion


def crear_rutas_de_la_clinica(*, horario: HorarioDeAtencion) -> Blueprint:
    """Información general de la clínica. El frontend dibuja la agenda con estos
    datos y así el horario está definido en un solo lugar (el dominio)."""
    rutas = Blueprint("clinica", __name__, url_prefix="/api")

    @rutas.get("/horario")
    def consultar_horario():
        return jsonify(
            {
                "apertura": f"{horario.apertura:%H:%M}",
                "cierre": f"{horario.cierre:%H:%M}",
                "duracionCitaMinutos": int(horario.duracion_cita.total_seconds() // 60),
                "diasHabiles": sorted(horario.dias_habiles),
                "descripcion": horario.descripcion(),
            }
        )

    @rutas.get("/salud")
    def salud():
        return jsonify({"estado": "ok"})

    return rutas
