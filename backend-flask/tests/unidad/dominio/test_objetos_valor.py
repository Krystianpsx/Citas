from datetime import datetime, timedelta

import pytest

from app.dominio.excepciones import DatoInvalido
from app.dominio.objetos_valor import DocumentoIdentidad, Email, IntervaloDeTiempo


class TestEmail:
    def test_normaliza_mayusculas_y_espacios(self):
        assert Email("  Ana.Torres@Correo.TEST ").valor == "ana.torres@correo.test"

    @pytest.mark.parametrize("invalido", ["", "ana", "ana@", "ana@correo", "a b@correo.com"])
    def test_rechaza_formatos_invalidos(self, invalido):
        with pytest.raises(DatoInvalido):
            Email(invalido)

    def test_dos_correos_iguales_son_el_mismo_valor(self):
        assert Email("ana@correo.test") == Email("ANA@correo.test")


class TestDocumentoIdentidad:
    def test_quita_puntos_y_guiones(self):
        assert DocumentoIdentidad("1.023.456-789").numero == "1023456789"

    @pytest.mark.parametrize("invalido", ["12345", "1234567890123", "12a456", ""])
    def test_rechaza_documentos_invalidos(self, invalido):
        with pytest.raises(DatoInvalido):
            DocumentoIdentidad(invalido)


class TestIntervaloDeTiempo:
    INICIO = datetime(2026, 11, 9, 9, 0)

    def test_no_permite_fin_antes_del_inicio(self):
        with pytest.raises(DatoInvalido):
            IntervaloDeTiempo(self.INICIO, self.INICIO)

    def test_detecta_solapamiento_parcial(self):
        a = IntervaloDeTiempo.desde(self.INICIO, timedelta(minutes=30))
        b = IntervaloDeTiempo.desde(self.INICIO + timedelta(minutes=15), timedelta(minutes=30))
        assert a.se_solapa_con(b) and b.se_solapa_con(a)

    def test_intervalos_contiguos_no_se_solapan(self):
        a = IntervaloDeTiempo.desde(self.INICIO, timedelta(minutes=30))
        b = IntervaloDeTiempo.desde(self.INICIO + timedelta(minutes=30), timedelta(minutes=30))
        assert not a.se_solapa_con(b)
