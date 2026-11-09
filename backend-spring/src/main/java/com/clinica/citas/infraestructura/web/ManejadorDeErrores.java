package com.clinica.citas.infraestructura.web;

import com.clinica.citas.dominio.excepciones.DatoInvalido;
import com.clinica.citas.dominio.excepciones.EntidadDuplicada;
import com.clinica.citas.dominio.excepciones.EntidadNoEncontrada;
import com.clinica.citas.dominio.excepciones.ErrorDeDominio;
import com.clinica.citas.dominio.excepciones.ReglaDeNegocioIncumplida;
import com.clinica.citas.infraestructura.web.dto.ErrorRespuesta;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Traduce errores a respuestas HTTP con el formato {"mensaje", "tipo"}.
 *
 * <p>Principio Abierto/Cerrado: para un error nuevo del dominio basta con agregarlo a la
 * tabla. La búsqueda recorre la jerarquía de la excepción, así una subclase
 * (por ejemplo TransicionDeEstadoInvalida) hereda el código de su padre.
 */
@RestControllerAdvice
public class ManejadorDeErrores {

    private static final Logger REGISTRO = LoggerFactory.getLogger(ManejadorDeErrores.class);

    private static final Map<Class<? extends RuntimeException>, HttpStatus> CODIGOS_HTTP = Map.of(
            SolicitudInvalida.class, HttpStatus.BAD_REQUEST,
            DatoInvalido.class, HttpStatus.BAD_REQUEST,
            EntidadNoEncontrada.class, HttpStatus.NOT_FOUND,
            EntidadDuplicada.class, HttpStatus.CONFLICT,
            ReglaDeNegocioIncumplida.class, HttpStatus.UNPROCESSABLE_ENTITY);

    @ExceptionHandler({ErrorDeDominio.class, SolicitudInvalida.class})
    public ResponseEntity<ErrorRespuesta> errorConocido(RuntimeException error) {
        return responder(codigoPara(error), error.getMessage(), error.getClass().getSimpleName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> cuerpoIlegible(HttpMessageNotReadableException error) {
        return responder(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición debe ser un objeto JSON válido.", "SolicitudInvalida");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorRespuesta> formatoNoSoportado(HttpMediaTypeNotSupportedException error) {
        return responder(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Envía la petición en formato JSON (Content-Type: application/json).", "SolicitudInvalida");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorRespuesta> rutaInexistente(NoResourceFoundException error) {
        return responder(HttpStatus.NOT_FOUND, "No existe la ruta solicitada.", "RutaNoEncontrada");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorRespuesta> metodoNoPermitido(HttpRequestMethodNotSupportedException error) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED,
                "Esta ruta no admite el método " + error.getMethod() + ".", "MetodoNoPermitido");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> errorInesperado(Exception error) {
        REGISTRO.error("Error no controlado", error);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado en el servidor.", "ErrorInterno");
    }

    private static HttpStatus codigoPara(Throwable error) {
        for (Class<?> clase = error.getClass(); clase != null; clase = clase.getSuperclass()) {
            HttpStatus codigo = CODIGOS_HTTP.get(clase);
            if (codigo != null) {
                return codigo;
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private static ResponseEntity<ErrorRespuesta> responder(HttpStatus codigo, String mensaje, String tipo) {
        return ResponseEntity.status(codigo).body(new ErrorRespuesta(mensaje, tipo));
    }
}
