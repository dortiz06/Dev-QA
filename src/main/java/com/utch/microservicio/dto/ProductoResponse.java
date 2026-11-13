package com.utch.microservicio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponse {

    private String id;
    private String nombre;
    private String codigo;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private Instant fechaCreacion;
}
