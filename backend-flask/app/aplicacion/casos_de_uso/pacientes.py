from app.aplicacion.comandos import RegistrarPacienteComando
from app.aplicacion.puertos.entrada import ListarPacientesCasoDeUso, RegistrarPacienteCasoDeUso
from app.aplicacion.puertos.salida import Reloj, RepositorioDePacientes
from app.dominio.excepciones import EntidadDuplicada
from app.dominio.objetos_valor import DocumentoIdentidad, Email
from app.dominio.paciente import Paciente


class RegistrarPacienteServicio(RegistrarPacienteCasoDeUso):
    def __init__(self, pacientes: RepositorioDePacientes, reloj: Reloj) -> None:
        self._pacientes = pacientes
        self._reloj = reloj

    def ejecutar(self, comando: RegistrarPacienteComando) -> Paciente:
        documento = DocumentoIdentidad(comando.documento)
        self._exigir_documento_libre(documento)
        paciente = Paciente.registrar(
            nombre=comando.nombre,
            documento=documento,
            email=Email(comando.email),
            telefono=comando.telefono,
            fecha_nacimiento=comando.fecha_nacimiento,
            hoy=self._reloj.hoy(),
        )
        self._pacientes.guardar(paciente)
        return paciente

    def _exigir_documento_libre(self, documento: DocumentoIdentidad) -> None:
        if self._pacientes.buscar_por_documento(documento) is not None:
            raise EntidadDuplicada(f"Ya existe un paciente con el documento {documento}.")


class ListarPacientesServicio(ListarPacientesCasoDeUso):
    def __init__(self, pacientes: RepositorioDePacientes) -> None:
        self._pacientes = pacientes

    def ejecutar(self) -> list[Paciente]:
        return self._pacientes.listar()
