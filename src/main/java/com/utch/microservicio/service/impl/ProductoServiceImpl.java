package com.utch.microservicio.service.impl;

import com.utch.microservicio.dto.ProductoRequest;
import com.utch.microservicio.dto.ProductoResponse;
import com.utch.microservicio.exception.DatoDuplicadoException;
import com.utch.microservicio.exception.RecursoNoEncontradoException;
import com.utch.microservicio.mapper.ProductoMapper;
import com.utch.microservicio.model.Producto;
import com.utch.microservicio.repository.ProductoRepository;
import com.utch.microservicio.service.ProductoService;
import com.utch.microservicio.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    @Override
    public ProductoResponse crear(ProductoRequest request) {
        validarCodigoDisponible(request.getCodigo(), null);
        Producto producto = productoMapper.toEntity(request);
        return guardar(producto);
    }

    @Override
    public List<ProductoResponse> listar() {
        return productoRepository.findAll().stream()
                .map(productoMapper::toResponse)
                .toList();
    }

    @Override
    public ProductoResponse buscarPorId(String id) {
        return productoMapper.toResponse(obtenerPorId(id));
    }

    @Override
    public ProductoResponse actualizar(String id, ProductoRequest request) {
        Producto existente = obtenerPorId(id);
        validarCodigoDisponible(request.getCodigo(), id);
        productoMapper.actualizarEntidad(existente, request);
        return guardar(existente);
    }

    @Override
    public void eliminar(String id) {
        if (!productoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException(id);
        }
        productoRepository.deleteById(id);
    }

    private Producto obtenerPorId(String id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(id));
    }

    private void validarCodigoDisponible(String codigo, String idActual) {
        boolean duplicado = idActual == null
                ? productoRepository.existsByCodigo(codigo)
                : productoRepository.existsByCodigoAndIdNot(codigo, idActual);
        if (duplicado) {
            throw new DatoDuplicadoException(AppConstants.ERROR_CODIGO_DUPLICADO);
        }
    }

    private ProductoResponse guardar(Producto producto) {
        try {
            return productoMapper.toResponse(productoRepository.save(producto));
        } catch (DuplicateKeyException ex) {
            throw new DatoDuplicadoException(AppConstants.ERROR_CODIGO_DUPLICADO);
        }
    }
}
