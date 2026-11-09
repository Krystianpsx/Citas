package com.clinica.citas.infraestructura.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Permite que el frontend, servido desde otro origen, llame a la API. */
@Configuration
public class ConfiguracionWeb implements WebMvcConfigurer {

    private final String[] origenesPermitidos;

    public ConfiguracionWeb(@Value("${citas.origenes-permitidos:http://localhost:5173}") String[] origenesPermitidos) {
        this.origenesPermitidos = origenesPermitidos;
    }

    @Override
    public void addCorsMappings(CorsRegistry registro) {
        registro.addMapping("/api/**").allowedOrigins(origenesPermitidos).allowedMethods("GET", "POST");
    }
}
