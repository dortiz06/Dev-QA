package com.utch.microservicio.model;

import com.utch.microservicio.util.AppConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = AppConstants.COLECCION_PRODUCTOS)
public class Producto {

    @Id
    private String id;
    private String nombre;
    @Indexed(unique = true)
    private String codigo;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private Instant fechaCreacion;
}
