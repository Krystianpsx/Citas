package com.clinica.citas.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.clinica.citas.dominio.excepciones.DatoInvalido;
import com.clinica.citas.dominio.modelo.DocumentoIdentidad;
import com.clinica.citas.dominio.modelo.Email;
import com.clinica.citas.dominio.modelo.IntervaloDeTiempo;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ObjetosDeValorTest {

    @Nested
    class CorreoElectronico {

        @Test
        void normaliza_mayusculas_y_espacios() {
            assertEquals("ana.torres@correo.test", new Email("  Ana.Torres@Correo.TEST ").valor());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "ana", "ana@", "ana@correo", "a b@correo.com"})
        void rechaza_formatos_invalidos(String invalido) {
            assertThrows(DatoInvalido.class, () -> new Email(invalido));
        }

        @Test
        void dos_correos_iguales_son_el_mismo_valor() {
            assertEquals(new Email("ana@correo.test"), new Email("ANA@correo.test"));
        }
    }

    @Nested
    class Documento {

        @Test
        void quita_puntos_y_guiones() {
            assertEquals("1023456789", new DocumentoIdentidad("1.023.456-789").numero());
        }

        @ParameterizedTest
        @ValueSource(strings = {"12345", "1234567890123", "12a456", ""})
        void rechaza_documentos_invalidos(String invalido) {
            assertThrows(DatoInvalido.class, () -> new DocumentoIdentidad(invalido));
        }
    }

    @Nested
    class Intervalo {

        private static final LocalDateTime NUEVE = LocalDateTime.of(2026, 11, 9, 9, 0);
        private static final Duration MEDIA_HORA = Duration.ofMinutes(30);

        @Test
        void no_permite_fin_igual_al_inicio() {
            assertThrows(DatoInvalido.class, () -> new IntervaloDeTiempo(NUEVE, NUEVE));
        }

        @Test
        void detecta_solapamiento_parcial() {
            var primero = IntervaloDeTiempo.desde(NUEVE, MEDIA_HORA);
            var segundo = IntervaloDeTiempo.desde(NUEVE.plusMinutes(15), MEDIA_HORA);
            assertTrue(primero.seSolapaCon(segundo));
            assertTrue(segundo.seSolapaCon(primero));
        }

        @Test
        void intervalos_contiguos_no_se_solapan() {
            var primero = IntervaloDeTiempo.desde(NUEVE, MEDIA_HORA);
            var segundo = IntervaloDeTiempo.desde(NUEVE.plusMinutes(30), MEDIA_HORA);
            assertFalse(primero.seSolapaCon(segundo));
        }
    }
}
