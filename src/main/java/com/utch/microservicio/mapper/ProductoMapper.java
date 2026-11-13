package com.utch.microservicio.mapper;

import com.utch.microservicio.dto.ProductoRequest;
import com.utch.microservicio.dto.ProductoResponse;
import com.utch.microservicio.model.Producto;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ProductoMapper {

    public Producto toEntity(ProductoRequest request) {
        return Producto.builder()
                .nombre(request.getNombre())
                .codigo(request.getCodigo())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .stock(request.getStock())
                .fechaCreacion(Instant.now())
                .build();
    }

    public ProductoResponse toResponse(Producto producto) {
        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .codigo(producto.getCodigo())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .fechaCreacion(producto.getFechaCreacion())
                .build();
    }

    public void actualizarEntidad(Producto destino, ProductoRequest request) {
        destino.setNombre(request.getNombre());
        destino.setCodigo(request.getCodigo());
        destino.setDescripcion(request.getDescripcion());
        destino.setPrecio(request.getPrecio());
        destino.setStock(request.getStock());
    }
}
