package com.utch.microservicio.service;

import com.utch.microservicio.dto.ProductoRequest;
import com.utch.microservicio.dto.ProductoResponse;

import java.util.List;

public interface ProductoService {

    ProductoResponse crear(ProductoRequest request);

    List<ProductoResponse> listar();

    ProductoResponse buscarPorId(String id);

    ProductoResponse actualizar(String id, ProductoRequest request);

    void eliminar(String id);
}
