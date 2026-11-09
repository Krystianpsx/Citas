"""Fábrica de la aplicación web (patrón Application Factory de Flask).

Construye el contenedor de dependencias, registra las rutas y los manejadores
de errores. Se inicia desde wsgi.py.
"""
from flask import Flask
from flask_cors import CORS

from app.aplicacion.puertos.salida import Notificador, Reloj
from app.infraestructura.configuracion.ajustes import Ajustes
from app.infraestructura.configuracion.contenedor import Contenedor
from app.infraestructura.configuracion.datos_de_ejemplo import cargar_datos_de_ejemplo
from app.infraestructura.web.errores import registrar_manejadores_de_errores
from app.infraestructura.web.rutas_citas import crear_rutas_de_citas
from app.infraestructura.web.rutas_clinica import crear_rutas_de_la_clinica
from app.infraestructura.web.rutas_medicos import crear_rutas_de_medicos
from app.infraestructura.web.rutas_pacientes import crear_rutas_de_pacientes


def crear_aplicacion(
    ajustes: Ajustes | None = None,
    *,
    reloj: Reloj | None = None,
    notificador: Notificador | None = None,
) -> Flask:
    ajustes = ajustes or Ajustes.desde_entorno()
    contenedor = Contenedor(ajustes, reloj=reloj, notificador=notificador)

    aplicacion = Flask(__name__)
    aplicacion.json.ensure_ascii = False
    aplicacion.json.sort_keys = False
    CORS(aplicacion, origins=list(ajustes.origenes_permitidos))

    _registrar_rutas(aplicacion, contenedor)
    registrar_manejadores_de_errores(aplicacion)
    _registrar_comandos(aplicacion, contenedor)
    return aplicacion


def _registrar_rutas(aplicacion: Flask, contenedor: Contenedor) -> None:
    aplicacion.register_blueprint(
        crear_rutas_de_pacientes(registrar=contenedor.registrar_paciente, listar=contenedor.listar_pacientes)
    )
    aplicacion.register_blueprint(
        crear_rutas_de_medicos(
            registrar=contenedor.registrar_medico,
            listar=contenedor.listar_medicos,
            consultar_disponibilidad=contenedor.consultar_disponibilidad,
        )
    )
    aplicacion.register_blueprint(
        crear_rutas_de_citas(
            agendar=contenedor.agendar_cita,
            buscar=contenedor.buscar_citas,
            confirmar=contenedor.confirmar_cita,
            atender=contenedor.atender_cita,
            cancelar=contenedor.cancelar_cita,
            reprogramar=contenedor.reprogramar_cita,
        )
    )
    aplicacion.register_blueprint(crear_rutas_de_la_clinica(horario=contenedor.horario))


def _registrar_comandos(aplicacion: Flask, contenedor: Contenedor) -> None:
    @aplicacion.cli.command("cargar-ejemplos")
    def cargar_ejemplos() -> None:
        """Carga médicos, pacientes y citas de demostración."""
        print(cargar_datos_de_ejemplo(contenedor))

