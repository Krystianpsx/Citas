package com.clinica.citas.infraestructura.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinica.citas.aplicacion.puertos.salida.Reloj;
import com.clinica.citas.soporte.EscenarioDePrueba;
import com.clinica.citas.soporte.RelojFijo;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Pruebas de extremo a extremo de la API: petición HTTP, controlador, caso de uso, dominio
 * y repositorio en memoria. El reloj se fija el lunes 9 de noviembre de 2026 a las 07:00.
 */
@SpringBootTest(properties = {
        "citas.persistencia=memoria",
        "citas.datos-de-ejemplo=false",
        "spring.datasource.url=jdbc:h2:mem:pruebas-api"})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ApiDeCitasTest {

    @TestConfiguration
    static class RelojDePrueba {

        @Bean
        @Primary
        Reloj relojFijo() {
            return new RelojFijo(EscenarioDePrueba.LUNES_TEMPRANO);
        }
    }

    @Autowired
    private MockMvc api;

    private String medicoId;
    private String pacienteId;

    @BeforeEach
    void registrarPersonas() throws Exception {
        medicoId = idDe(enviar("/api/medicos", """
                {"nombre": "Dra. Lucía", "especialidad": "PEDIATRIA", "registroProfesional": "rm-1", "email": "l@c.test"}
                """));
        pacienteId = idDe(enviar("/api/pacientes", """
                {"nombre": "Ana Torres", "documento": "1023456789", "email": "ana@correo.test"}
                """));
    }

    @Test
    void flujo_completo_de_una_cita() throws Exception {
        ResultActions creada = enviar("/api/citas", citaA("2026-11-09T10:00", "Fiebre"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"))
                .andExpect(jsonPath("$.fin").value("2026-11-09T10:30"))
                .andExpect(jsonPath("$.medico.especialidad.nombre").value("Pediatría"));
        String citaId = idDe(creada);

        api.perform(post("/api/citas/{id}/confirmar", citaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));

        api.perform(get("/api/citas").param("fecha", "2026-11-09"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(citaId));

        String horasLibres = contenido(api.perform(
                get("/api/medicos/{id}/disponibilidad", medicoId).param("fecha", "2026-11-09")));
        assertFalse(horasLibres.contains("2026-11-09T10:00"));
        assertTrue(horasLibres.contains("2026-11-09T10:30"));

        enviar("/api/citas/" + citaId + "/cancelar", "{\"motivo\": \"Mejoró\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"))
                .andExpect(jsonPath("$.motivoCancelacion").value("Mejoró"));
    }

    @Test
    void una_regla_de_negocio_incumplida_responde_422_con_un_mensaje_claro() throws Exception {
        ResultActions respuesta = enviar("/api/citas", citaA("2026-11-14T10:00", "Sábado"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.tipo").value("ReglaDeNegocioIncumplida"));
        assertTrue(contenido(respuesta).contains("lunes a viernes"));
    }

    @Test
    void un_cruce_de_horario_responde_422() throws Exception {
        enviar("/api/citas", citaA("2026-11-09T10:00", "Primera")).andExpect(status().isCreated());
        enviar("/api/citas", citaA("2026-11-09T10:00", "Segunda")).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void los_datos_mal_formados_responden_400() throws Exception {
        enviar("/api/citas", citaA("mañana", "X")).andExpect(status().isBadRequest());
        enviar("/api/citas", "no es json").andExpect(status().isBadRequest());
        enviar("/api/pacientes", "{\"nombre\": \"X\", \"documento\": \"1\", \"email\": \"x@y.co\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void un_recurso_inexistente_responde_404() throws Exception {
        api.perform(post("/api/citas/no-existe/confirmar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.tipo").value("EntidadNoEncontrada"));
    }

    @Test
    void un_documento_duplicado_responde_409() throws Exception {
        enviar("/api/pacientes", "{\"nombre\": \"Otra\", \"documento\": \"1023456789\", \"email\": \"otra@correo.test\"}")
                .andExpect(status().isConflict());
    }

    @Test
    void entrega_el_horario_de_atencion() throws Exception {
        api.perform(get("/api/horario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apertura").value("08:00"))
                .andExpect(jsonPath("$.cierre").value("18:00"))
                .andExpect(jsonPath("$.duracionCitaMinutos").value(30))
                .andExpect(jsonPath("$.diasHabiles.length()").value(5))
                .andExpect(jsonPath("$.descripcion").value("de lunes a viernes, de 08:00 a 18:00"));
    }

    @Test
    void lista_las_especialidades() throws Exception {
        String especialidades = contenido(api.perform(get("/api/medicos/especialidades")));
        assertTrue(especialidades.contains("{\"codigo\":\"CARDIOLOGIA\",\"nombre\":\"Cardiología\"}"));
    }

    @Test
    void una_ruta_inexistente_responde_404_en_json() throws Exception {
        api.perform(get("/api/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    private String citaA(String inicio, String motivo) {
        return """
                {"pacienteId": "%s", "medicoId": "%s", "inicio": "%s", "motivo": "%s"}
                """.formatted(pacienteId, medicoId, inicio, motivo);
    }

    private ResultActions enviar(String ruta, String json) throws Exception {
        return api.perform(post(ruta).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static String contenido(ResultActions respuesta) throws Exception {
        return respuesta.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private static String idDe(ResultActions respuesta) throws Exception {
        return JsonPath.read(contenido(respuesta), "$.id");
    }
}
