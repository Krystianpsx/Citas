from datetime import datetime

from app.aplicacion.puertos.salida import Reloj


class RelojDelSistema(Reloj):
    def ahora(self) -> datetime:
        return datetime.now().replace(microsecond=0)
