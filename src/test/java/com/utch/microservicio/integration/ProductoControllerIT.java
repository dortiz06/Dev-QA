package com.utch.microservicio.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utch.microservicio.dto.ProductoRequest;
import com.utch.microservicio.model.Producto;
import com.utch.microservicio.repository.ProductoRepository;
import com.utch.microservicio.support.ProductoFixtures;
import com.utch.microservicio.util.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.math.BigDecimal;
import java.time.Instant;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ProductoControllerIT {

    static {
        String dockerSock = System.getProperty("user.home") + "/.docker/run/docker.sock";
        if (new File(dockerSock).exists()) {
            System.setProperty("DOCKER_HOST", "unix://" + dockerSock);
        }
        System.setProperty("api.version", "1.44");
    }

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductoRepository productoRepository;

    @BeforeEach
    void limpiar() {
        productoRepository.deleteAll();
    }

    @Test
    void crear_debeRetornar201YElProductoPersistido() throws Exception {
        mockMvc.perform(post(AppConstants.API_PRODUCTOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ProductoFixtures.request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nombre").value(ProductoFixtures.NOMBRE))
                .andExpect(jsonPath("$.codigo").value(ProductoFixtures.CODIGO))
                .andExpect(jsonPath("$.descripcion").value(ProductoFixtures.DESCRIPCION))
                .andExpect(jsonPath("$.precio").value(18999.50))
                .andExpect(jsonPath("$.stock").value(ProductoFixtures.STOCK))
                .andExpect(jsonPath("$.fechaCreacion").isNotEmpty());
    }

    @Test
    void crear_debeRetornar400CuandoElCuerpoEsInvalido() throws Exception {
        ProductoRequest invalido = ProductoRequest.builder()
                .nombre("")
                .codigo("??")
                .precio(BigDecimal.ZERO)
                .stock(-4)
                .build();

        mockMvc.perform(post(AppConstants.API_PRODUCTOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.mensaje").value(AppConstants.ERROR_VALIDACION))
                .andExpect(jsonPath("$.path").value(AppConstants.API_PRODUCTOS))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.errores").isArray());
    }

    @Test
    void crear_debeRetornar409CuandoElCodigoYaExiste() throws Exception {
        productoRepository.save(ProductoFixtures.entidad());

        mockMvc.perform(post(AppConstants.API_PRODUCTOS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ProductoFixtures.request())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.mensaje").value(AppConstants.ERROR_CODIGO_DUPLICADO))
                .andExpect(jsonPath("$.path").value(AppConstants.API_PRODUCTOS));
    }

    @Test
    void listar_debeRetornar200ConLosProductos() throws Exception {
        productoRepository.save(ProductoFixtures.entidad());

        mockMvc.perform(get(AppConstants.API_PRODUCTOS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigo").value(ProductoFixtures.CODIGO));
    }

    @Test
    void buscarPorId_debeRetornar200CuandoExiste() throws Exception {
        Producto guardado = productoRepository.save(ProductoFixtures.entidad());

        mockMvc.perform(get(AppConstants.API_PRODUCTOS + "/" + guardado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardado.getId()))
                .andExpect(jsonPath("$.nombre").value(ProductoFixtures.NOMBRE));
    }

    @Test
    void buscarPorId_debeRetornar404CuandoNoExiste() throws Exception {
        mockMvc.perform(get(AppConstants.API_PRODUCTOS + "/inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value(AppConstants.API_PRODUCTOS + "/inexistente"));
    }

    @Test
    void actualizar_debeRetornar200CuandoLosDatosSonValidos() throws Exception {
        Producto guardado = productoRepository.save(ProductoFixtures.entidad());
        ProductoRequest request = ProductoFixtures.request("LAP999");
        request.setNombre("Laptop Actualizada");
        request.setStock(7);

        mockMvc.perform(put(AppConstants.API_PRODUCTOS + "/" + guardado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Laptop Actualizada"))
                .andExpect(jsonPath("$.codigo").value("LAP999"))
                .andExpect(jsonPath("$.stock").value(7));
    }

    @Test
    void actualizar_debeRetornar400CuandoLaValidacionFalla() throws Exception {
        Producto guardado = productoRepository.save(ProductoFixtures.entidad());
        ProductoRequest invalido = ProductoFixtures.request();
        invalido.setNombre("x");

        mockMvc.perform(put(AppConstants.API_PRODUCTOS + "/" + guardado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errores").isArray());
    }

    @Test
    void actualizar_debeRetornar404CuandoNoExiste() throws Exception {
        mockMvc.perform(put(AppConstants.API_PRODUCTOS + "/inexistente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ProductoFixtures.request())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void actualizar_debeRetornar409CuandoElCodigoPerteneceAOtroProducto() throws Exception {
        productoRepository.save(Producto.builder()
                .nombre("Teclado")
                .codigo("TEC001")
                .descripcion("Mecanico")
                .precio(new BigDecimal("1200"))
                .stock(4)
                .fechaCreacion(Instant.now())
                .build());
        Producto actualizable = productoRepository.save(ProductoFixtures.entidad());
        ProductoRequest request = ProductoFixtures.request("TEC001");

        mockMvc.perform(put(AppConstants.API_PRODUCTOS + "/" + actualizable.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensaje").value(AppConstants.ERROR_CODIGO_DUPLICADO));
    }

    @Test
    void eliminar_debeRetornar204YQuitarElProducto() throws Exception {
        Producto guardado = productoRepository.save(ProductoFixtures.entidad());

        mockMvc.perform(delete(AppConstants.API_PRODUCTOS + "/" + guardado.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(AppConstants.API_PRODUCTOS + "/" + guardado.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminar_debeRetornar404CuandoNoExiste() throws Exception {
        mockMvc.perform(delete(AppConstants.API_PRODUCTOS + "/inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
