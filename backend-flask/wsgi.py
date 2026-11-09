"""Punto de entrada para Flask y para servidores WSGI (gunicorn, waitress).

    flask --app wsgi run --debug
    flask --app wsgi cargar-ejemplos
"""
import logging

from app.principal import crear_aplicacion

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s")

application = crear_aplicacion()
