package com.utch.microservicio.support;

import com.utch.microservicio.dto.ProductoRequest;
import com.utch.microservicio.model.Producto;

import java.math.BigDecimal;
import java.time.Instant;

public final class ProductoFixtures {

    public static final String ID = "abc123";
    public static final String NOMBRE = "Laptop Gamer";
    public static final String CODIGO = "LAP001";
    public static final String DESCRIPCION = "Equipo de alto rendimiento";
    public static final BigDecimal PRECIO = new BigDecimal("18999.50");
    public static final Integer STOCK = 10;

    private ProductoFixtures() {
    }

    public static ProductoRequest request() {
        return request(CODIGO);
    }

    public static ProductoRequest request(String codigo) {
        return ProductoRequest.builder()
                .nombre(NOMBRE)
                .codigo(codigo)
                .descripcion(DESCRIPCION)
                .precio(PRECIO)
                .stock(STOCK)
                .build();
    }

    public static Producto entidad() {
        return Producto.builder()
                .id(ID)
                .nombre(NOMBRE)
                .codigo(CODIGO)
                .descripcion(DESCRIPCION)
                .precio(PRECIO)
                .stock(STOCK)
                .fechaCreacion(Instant.parse("2024-01-15T10:00:00Z"))
                .build();
    }
}
