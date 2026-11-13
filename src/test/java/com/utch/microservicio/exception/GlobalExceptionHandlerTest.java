package com.utch.microservicio.exception;

import com.utch.microservicio.controller.ProductoController;
import com.utch.microservicio.dto.ErrorResponse;
import com.utch.microservicio.dto.ProductoRequest;
import com.utch.microservicio.util.AppConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private static final String PATH = AppConstants.API_PRODUCTOS;

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(PATH);
    }

    @Test
    void manejarNoEncontrado_debeResponder404() {
        RecursoNoEncontradoException ex = new RecursoNoEncontradoException("xyz");

        ResponseEntity<ErrorResponse> response = handler.manejarNoEncontrado(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMensaje()).contains("xyz");
        assertThat(response.getBody().getPath()).isEqualTo(PATH);
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getErrores()).isNull();
    }

    @Test
    void manejarDuplicado_debeResponder409() {
        DatoDuplicadoException ex = new DatoDuplicadoException(AppConstants.ERROR_CODIGO_DUPLICADO);

        ResponseEntity<ErrorResponse> response = handler.manejarDuplicado(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMensaje()).isEqualTo(AppConstants.ERROR_CODIGO_DUPLICADO);
        assertThat(response.getBody().getPath()).isEqualTo(PATH);
    }

    @Test
    void manejarValidacion_debeResponder400ConErroresPorCampo() throws Exception {
        Method method = ProductoController.class.getMethod("crear", ProductoRequest.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new ProductoRequest(), "request");
        bindingResult.addError(new FieldError("request", "nombre", AppConstants.MSG_NOMBRE_BLANK));
        bindingResult.addError(new FieldError("request", "precio", AppConstants.MSG_PRECIO_POSITIVO));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.manejarValidacion(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMensaje()).isEqualTo(AppConstants.ERROR_VALIDACION);
        assertThat(response.getBody().getErrores()).hasSize(2);
        assertThat(response.getBody().getErrores())
                .extracting("campo")
                .containsExactly("nombre", "precio");
    }

    @Test
    void manejarGenerico_debeResponder500() {
        ResponseEntity<ErrorResponse> response =
                handler.manejarGenerico(new IllegalStateException("fallo inesperado"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMensaje()).isEqualTo(AppConstants.ERROR_INTERNO);
        assertThat(response.getBody().getPath()).isEqualTo(PATH);
        assertThat(response.getBody().getErrores()).isNull();
    }
}
