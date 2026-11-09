"""Traducción entre filas de la base de datos y entidades del dominio."""
from app.dominio.cita import Cita
from app.dominio.estado_cita import EstadoCita
from app.dominio.medico import Especialidad, Medico
from app.dominio.objetos_valor import DocumentoIdentidad, Email, IntervaloDeTiempo
from app.dominio.paciente import Paciente
from app.infraestructura.persistencia.sql.modelos import CitaFila, MedicoFila, PacienteFila


def paciente_a_fila(paciente: Paciente) -> PacienteFila:
    return PacienteFila(
        id=paciente.id,
        nombre=paciente.nombre,
        documento=paciente.documento.numero,
        email=paciente.email.valor,
        telefono=paciente.telefono,
        fecha_nacimiento=paciente.fecha_nacimiento,
    )


def paciente_desde_fila(fila: PacienteFila) -> Paciente:
    return Paciente(
        id=fila.id,
        nombre=fila.nombre,
        documento=DocumentoIdentidad(fila.documento),
        email=Email(fila.email),
        telefono=fila.telefono,
        fecha_nacimiento=fila.fecha_nacimiento,
    )


def medico_a_fila(medico: Medico) -> MedicoFila:
    return MedicoFila(
        id=medico.id,
        nombre=medico.nombre,
        especialidad=medico.especialidad.value,
        registro_profesional=medico.registro_profesional,
        email=medico.email.valor,
    )


def medico_desde_fila(fila: MedicoFila) -> Medico:
    return Medico(
        id=fila.id,
        nombre=fila.nombre,
        especialidad=Especialidad(fila.especialidad),
        registro_profesional=fila.registro_profesional,
        email=Email(fila.email),
    )


def cita_a_fila(cita: Cita) -> CitaFila:
    return CitaFila(
        id=cita.id,
        paciente_id=cita.paciente_id,
        medico_id=cita.medico_id,
        inicio=cita.inicio,
        fin=cita.fin,
        motivo=cita.motivo,
        estado=cita.estado.value,
        motivo_cancelacion=cita.motivo_cancelacion,
    )


def cita_desde_fila(fila: CitaFila) -> Cita:
    return Cita(
        id=fila.id,
        paciente_id=fila.paciente_id,
        medico_id=fila.medico_id,
        intervalo=IntervaloDeTiempo(inicio=fila.inicio, fin=fila.fin),
        motivo=fila.motivo,
        estado=EstadoCita(fila.estado),
        motivo_cancelacion=fila.motivo_cancelacion,
    )
