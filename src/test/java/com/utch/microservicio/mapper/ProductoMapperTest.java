package com.utch.microservicio.mapper;

import com.utch.microservicio.dto.ProductoRequest;
import com.utch.microservicio.dto.ProductoResponse;
import com.utch.microservicio.model.Producto;
import com.utch.microservicio.support.ProductoFixtures;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductoMapperTest {

    private final ProductoMapper mapper = new ProductoMapper();

    @Test
    void toEntity_debeCopiarCamposYAsignarFecha() {
        Producto producto = mapper.toEntity(ProductoFixtures.request());

        assertThat(producto.getId()).isNull();
        assertThat(producto.getNombre()).isEqualTo(ProductoFixtures.NOMBRE);
        assertThat(producto.getCodigo()).isEqualTo(ProductoFixtures.CODIGO);
        assertThat(producto.getDescripcion()).isEqualTo(ProductoFixtures.DESCRIPCION);
        assertThat(producto.getPrecio()).isEqualByComparingTo(ProductoFixtures.PRECIO);
        assertThat(producto.getStock()).isEqualTo(ProductoFixtures.STOCK);
        assertThat(producto.getFechaCreacion()).isNotNull();
    }

    @Test
    void toResponse_debeCopiarTodosLosCampos() {
        Producto entidad = ProductoFixtures.entidad();

        ProductoResponse response = mapper.toResponse(entidad);

        assertThat(response.getId()).isEqualTo(entidad.getId());
        assertThat(response.getNombre()).isEqualTo(entidad.getNombre());
        assertThat(response.getCodigo()).isEqualTo(entidad.getCodigo());
        assertThat(response.getDescripcion()).isEqualTo(entidad.getDescripcion());
        assertThat(response.getPrecio()).isEqualByComparingTo(entidad.getPrecio());
        assertThat(response.getStock()).isEqualTo(entidad.getStock());
        assertThat(response.getFechaCreacion()).isEqualTo(entidad.getFechaCreacion());
    }

    @Test
    void actualizarEntidad_debeReemplazarCamposSinTocarIdNiFecha() {
        Producto destino = ProductoFixtures.entidad();
        ProductoRequest request = ProductoRequest.builder()
                .nombre("Mouse")
                .codigo("MOU99")
                .descripcion("Inalambrico")
                .precio(new BigDecimal("499.99"))
                .stock(3)
                .build();

        mapper.actualizarEntidad(destino, request);

        assertThat(destino.getId()).isEqualTo(ProductoFixtures.ID);
        assertThat(destino.getFechaCreacion()).isEqualTo(ProductoFixtures.entidad().getFechaCreacion());
        assertThat(destino.getNombre()).isEqualTo("Mouse");
        assertThat(destino.getCodigo()).isEqualTo("MOU99");
        assertThat(destino.getDescripcion()).isEqualTo("Inalambrico");
        assertThat(destino.getPrecio()).isEqualByComparingTo("499.99");
        assertThat(destino.getStock()).isEqualTo(3);
    }
}
