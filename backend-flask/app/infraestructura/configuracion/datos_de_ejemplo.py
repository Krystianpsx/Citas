"""Datos de demostración. Se cargan a través de los mismos casos de uso que usa
la API, así respetan todas las reglas del negocio."""
from datetime import date, datetime, time, timedelta

from app.aplicacion.comandos import AgendarCitaComando, RegistrarMedicoComando, RegistrarPacienteComando
from app.infraestructura.configuracion.contenedor import Contenedor

_MEDICOS = [
    RegistrarMedicoComando("Dra. Lucía Fernández", "MEDICINA_GENERAL", "RM-10234", "lucia.fernandez@clinica.test"),
    RegistrarMedicoComando("Dr. Andrés Salazar", "CARDIOLOGIA", "RM-20871", "andres.salazar@clinica.test"),
    RegistrarMedicoComando("Dra. Camila Rojas", "PEDIATRIA", "RM-31560", "camila.rojas@clinica.test"),
    RegistrarMedicoComando("Dr. Mateo Vargas", "DERMATOLOGIA", "RM-44102", "mateo.vargas@clinica.test"),
]

_PACIENTES = [
    RegistrarPacienteComando("Ana María Torres", "1023456789", "ana.torres@correo.test", "3001234567", date(1990, 4, 12)),
    RegistrarPacienteComando("Jorge Luis Ramírez", "80123456", "jorge.ramirez@correo.test", "3109876543", date(1978, 9, 3)),
    RegistrarPacienteComando("Valentina Gómez", "1098765432", "valentina.gomez@correo.test", None, date(2015, 1, 22)),
    RegistrarPacienteComando("Carlos Mejía", "79555123", "carlos.mejia@correo.test", "3205551234", date(1965, 11, 30)),
    RegistrarPacienteComando("Sofía Herrera", "1012345678", "sofia.herrera@correo.test", "3157778899", date(2001, 6, 8)),
]

# (paciente, médico, hora, motivo) -> índices en las listas anteriores
_CITAS = [
    (0, 0, time(8, 30), "Control anual y revisión de exámenes"),
    (1, 1, time(9, 0), "Dolor en el pecho al hacer ejercicio"),
    (2, 2, time(9, 0), "Control de crecimiento y vacunas"),
    (3, 1, time(10, 30), "Seguimiento de hipertensión"),
    (4, 3, time(11, 0), "Revisión de lunar en la espalda"),
    (0, 3, time(14, 0), "Brote en las manos"),
    (3, 0, time(15, 30), "Renovación de fórmula médica"),
]


def cargar_datos_de_ejemplo(contenedor: Contenedor) -> str:
    if contenedor.listar_medicos.ejecutar():
        return "La base de datos ya tiene información; no se cargó nada."
    medicos = [contenedor.registrar_medico.ejecutar(comando) for comando in _MEDICOS]
    pacientes = [contenedor.registrar_paciente.ejecutar(comando) for comando in _PACIENTES]
    dia = _proximo_dia_habil(contenedor.reloj.hoy())
    for indice_paciente, indice_medico, hora, motivo in _CITAS:
        contenedor.agendar_cita.ejecutar(
            AgendarCitaComando(
                paciente_id=pacientes[indice_paciente].id,
                medico_id=medicos[indice_medico].id,
                inicio=datetime.combine(dia, hora),
                motivo=motivo,
            )
        )
    return f"Se cargaron {len(medicos)} médicos, {len(pacientes)} pacientes y {len(_CITAS)} citas para el {dia}."


def _proximo_dia_habil(hoy: date) -> date:
    dia = hoy + timedelta(days=1)
    while dia.weekday() >= 5:
        dia += timedelta(days=1)
    return dia
