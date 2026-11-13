package com.utch.microservicio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utch.microservicio.dto.ProductoRequest;
import com.utch.microservicio.dto.ProductoResponse;
import com.utch.microservicio.exception.DatoDuplicadoException;
import com.utch.microservicio.exception.RecursoNoEncontradoException;
import com.utch.microservicio.service.ProductoService;
import com.utch.microservicio.support.ProductoFixtures;
import com.utch.microservicio.util.AppConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductoService productoService;

    @Test
    void crear_debeRetornar201() throws Exception {
        when(productoService.crear(any(ProductoRequest.class))).thenReturn(respuesta());

        mockMvc.perform(post(AppConstants.API_PRODUCTOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ProductoFixtures.request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ProductoFixtures.ID))
                .andExpect(jsonPath("$.codigo").value(ProductoFixtures.CODIGO));
    }

    @Test
    void crear_debeRetornar400CuandoLaValidacionFalla() throws Exception {
        ProductoRequest invalido = ProductoFixtures.request();
        invalido.setNombre("ab");
        invalido.setCodigo("LAP-001");
        invalido.setPrecio(ProductoFixtures.PRECIO.negate());
        invalido.setStock(-1);

        mockMvc.perform(post(AppConstants.API_PRODUCTOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.mensaje").value(AppConstants.ERROR_VALIDACION))
                .andExpect(jsonPath("$.path").value(AppConstants.API_PRODUCTOS))
                .andExpect(jsonPath("$.errores").isArray());
    }

    @Test
    void crear_debeRetornar409CuandoElCodigoEstaDuplicado() throws Exception {
        when(productoService.crear(any(ProductoRequest.class)))
                .thenThrow(new DatoDuplicadoException(AppConstants.ERROR_CODIGO_DUPLICADO));

        mockMvc.perform(post(AppConstants.API_PRODUCTOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ProductoFixtures.request())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensaje").value(AppConstants.ERROR_CODIGO_DUPLICADO));
    }

    @Test
    void listar_debeRetornar200() throws Exception {
        when(productoService.listar()).thenReturn(List.of(respuesta()));

        mockMvc.perform(get(AppConstants.API_PRODUCTOS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ProductoFixtures.ID));
    }

    @Test
    void buscarPorId_debeRetornar200() throws Exception {
        when(productoService.buscarPorId(ProductoFixtures.ID)).thenReturn(respuesta());

        mockMvc.perform(get(AppConstants.API_PRODUCTOS + "/" + ProductoFixtures.ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value(ProductoFixtures.NOMBRE));
    }

    @Test
    void buscarPorId_debeRetornar404() throws Exception {
        when(productoService.buscarPorId(ProductoFixtures.ID))
                .thenThrow(new RecursoNoEncontradoException(ProductoFixtures.ID));

        mockMvc.perform(get(AppConstants.API_PRODUCTOS + "/" + ProductoFixtures.ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value(AppConstants.API_PRODUCTOS + "/" + ProductoFixtures.ID));
    }

    @Test
    void actualizar_debeRetornar200() throws Exception {
        when(productoService.actualizar(eq(ProductoFixtures.ID), any(ProductoRequest.class)))
                .thenReturn(respuesta());

        mockMvc.perform(put(AppConstants.API_PRODUCTOS + "/" + ProductoFixtures.ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ProductoFixtures.request())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ProductoFixtures.ID));
    }

    @Test
    void eliminar_debeRetornar204() throws Exception {
        doNothing().when(productoService).eliminar(ProductoFixtures.ID);

        mockMvc.perform(delete(AppConstants.API_PRODUCTOS + "/" + ProductoFixtures.ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminar_debeRetornar404() throws Exception {
        doThrow(new RecursoNoEncontradoException(ProductoFixtures.ID))
                .when(productoService).eliminar(ProductoFixtures.ID);

        mockMvc.perform(delete(AppConstants.API_PRODUCTOS + "/" + ProductoFixtures.ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void listar_debeRetornar500CuandoOcurreUnErrorInesperado() throws Exception {
        when(productoService.listar()).thenThrow(new IllegalStateException("fallo"));

        mockMvc.perform(get(AppConstants.API_PRODUCTOS))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.mensaje").value(AppConstants.ERROR_INTERNO));
    }

    private ProductoResponse respuesta() {
        return ProductoResponse.builder()
                .id(ProductoFixtures.ID)
                .nombre(ProductoFixtures.NOMBRE)
                .codigo(ProductoFixtures.CODIGO)
                .descripcion(ProductoFixtures.DESCRIPCION)
                .precio(ProductoFixtures.PRECIO)
                .stock(ProductoFixtures.STOCK)
                .fechaCreacion(Instant.parse("2024-01-15T10:00:00Z"))
                .build();
    }
}
