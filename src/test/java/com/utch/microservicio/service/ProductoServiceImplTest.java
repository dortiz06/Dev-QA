package com.utch.microservicio.service;

import com.utch.microservicio.dto.ProductoRequest;
import com.utch.microservicio.dto.ProductoResponse;
import com.utch.microservicio.exception.DatoDuplicadoException;
import com.utch.microservicio.exception.RecursoNoEncontradoException;
import com.utch.microservicio.mapper.ProductoMapper;
import com.utch.microservicio.model.Producto;
import com.utch.microservicio.repository.ProductoRepository;
import com.utch.microservicio.service.impl.ProductoServiceImpl;
import com.utch.microservicio.support.ProductoFixtures;
import com.utch.microservicio.util.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    private ProductoServiceImpl productoService;

    @BeforeEach
    void setUp() {
        productoService = new ProductoServiceImpl(productoRepository, new ProductoMapper());
    }

    @Test
    void crear_debePersistirProductoCuandoElCodigoEsUnico() {
        ProductoRequest request = ProductoFixtures.request();
        when(productoRepository.existsByCodigo(ProductoFixtures.CODIGO)).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto producto = invocation.getArgument(0);
            producto.setId(ProductoFixtures.ID);
            return producto;
        });

        ProductoResponse response = productoService.crear(request);

        assertThat(response.getId()).isEqualTo(ProductoFixtures.ID);
        assertThat(response.getNombre()).isEqualTo(ProductoFixtures.NOMBRE);
        assertThat(response.getCodigo()).isEqualTo(ProductoFixtures.CODIGO);
        assertThat(response.getPrecio()).isEqualByComparingTo(ProductoFixtures.PRECIO);
        assertThat(response.getFechaCreacion()).isNotNull();
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void crear_debeLanzarDuplicadoCuandoElCodigoYaExiste() {
        when(productoRepository.existsByCodigo(ProductoFixtures.CODIGO)).thenReturn(true);

        assertThatThrownBy(() -> productoService.crear(ProductoFixtures.request()))
                .isInstanceOf(DatoDuplicadoException.class)
                .hasMessage(AppConstants.ERROR_CODIGO_DUPLICADO);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void crear_debeLanzarDuplicadoCuandoMongoReportaIndiceUnico() {
        when(productoRepository.existsByCodigo(ProductoFixtures.CODIGO)).thenReturn(false);
        when(productoRepository.save(any(Producto.class)))
                .thenThrow(new DuplicateKeyException("codigo duplicado"));

        assertThatThrownBy(() -> productoService.crear(ProductoFixtures.request()))
                .isInstanceOf(DatoDuplicadoException.class)
                .hasMessage(AppConstants.ERROR_CODIGO_DUPLICADO);
    }

    @Test
    void listar_debeRetornarTodosLosProductosMapeados() {
        when(productoRepository.findAll()).thenReturn(List.of(ProductoFixtures.entidad()));

        List<ProductoResponse> productos = productoService.listar();

        assertThat(productos).hasSize(1);
        assertThat(productos.get(0).getId()).isEqualTo(ProductoFixtures.ID);
        assertThat(productos.get(0).getCodigo()).isEqualTo(ProductoFixtures.CODIGO);
    }

    @Test
    void listar_debeRetornarListaVaciaCuandoNoHayProductos() {
        when(productoRepository.findAll()).thenReturn(List.of());

        assertThat(productoService.listar()).isEmpty();
    }

    @Test
    void buscarPorId_debeRetornarProductoCuandoExiste() {
        when(productoRepository.findById(ProductoFixtures.ID))
                .thenReturn(Optional.of(ProductoFixtures.entidad()));

        ProductoResponse response = productoService.buscarPorId(ProductoFixtures.ID);

        assertThat(response.getId()).isEqualTo(ProductoFixtures.ID);
        assertThat(response.getNombre()).isEqualTo(ProductoFixtures.NOMBRE);
    }

    @Test
    void buscarPorId_debeLanzarNoEncontradoCuandoNoExiste() {
        when(productoRepository.findById(ProductoFixtures.ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.buscarPorId(ProductoFixtures.ID))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage(String.format(AppConstants.ERROR_PRODUCTO_NO_ENCONTRADO, ProductoFixtures.ID));
    }

    @Test
    void actualizar_debeModificarProductoCuandoExisteYElCodigoEsValido() {
        Producto existente = ProductoFixtures.entidad();
        ProductoRequest request = ProductoRequest.builder()
                .nombre("Laptop Pro")
                .codigo("LAP002")
                .descripcion("Actualizado")
                .precio(new BigDecimal("20000.00"))
                .stock(5)
                .build();
        when(productoRepository.findById(ProductoFixtures.ID)).thenReturn(Optional.of(existente));
        when(productoRepository.existsByCodigoAndIdNot("LAP002", ProductoFixtures.ID)).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductoResponse response = productoService.actualizar(ProductoFixtures.ID, request);

        assertThat(response.getNombre()).isEqualTo("Laptop Pro");
        assertThat(response.getCodigo()).isEqualTo("LAP002");
        assertThat(response.getStock()).isEqualTo(5);
        assertThat(response.getFechaCreacion()).isEqualTo(existente.getFechaCreacion());
    }

    @Test
    void actualizar_debeLanzarNoEncontradoCuandoNoExiste() {
        when(productoRepository.findById(ProductoFixtures.ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.actualizar(ProductoFixtures.ID, ProductoFixtures.request()))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void actualizar_debeLanzarDuplicadoCuandoElCodigoPerteneceAOtroProducto() {
        when(productoRepository.findById(ProductoFixtures.ID))
                .thenReturn(Optional.of(ProductoFixtures.entidad()));
        when(productoRepository.existsByCodigoAndIdNot(ProductoFixtures.CODIGO, ProductoFixtures.ID))
                .thenReturn(true);

        assertThatThrownBy(() -> productoService.actualizar(ProductoFixtures.ID, ProductoFixtures.request()))
                .isInstanceOf(DatoDuplicadoException.class)
                .hasMessage(AppConstants.ERROR_CODIGO_DUPLICADO);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void actualizar_debeLanzarDuplicadoCuandoMongoReportaIndiceUnico() {
        when(productoRepository.findById(ProductoFixtures.ID))
                .thenReturn(Optional.of(ProductoFixtures.entidad()));
        when(productoRepository.existsByCodigoAndIdNot(anyString(), anyString())).thenReturn(false);
        when(productoRepository.save(any(Producto.class)))
                .thenThrow(new DuplicateKeyException("codigo duplicado"));

        assertThatThrownBy(() -> productoService.actualizar(ProductoFixtures.ID, ProductoFixtures.request()))
                .isInstanceOf(DatoDuplicadoException.class);
    }

    @Test
    void eliminar_debeBorrarCuandoElProductoExiste() {
        when(productoRepository.existsById(ProductoFixtures.ID)).thenReturn(true);

        productoService.eliminar(ProductoFixtures.ID);

        verify(productoRepository).deleteById(ProductoFixtures.ID);
    }

    @Test
    void eliminar_debeLanzarNoEncontradoCuandoNoExiste() {
        when(productoRepository.existsById(ProductoFixtures.ID)).thenReturn(false);

        assertThatThrownBy(() -> productoService.eliminar(ProductoFixtures.ID))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(productoRepository, never()).deleteById(anyString());
    }
}
