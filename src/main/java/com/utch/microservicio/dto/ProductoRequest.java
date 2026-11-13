package com.utch.microservicio.dto;

import com.utch.microservicio.util.AppConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequest {

    @NotBlank(message = AppConstants.MSG_NOMBRE_BLANK)
    @Size(min = 3, max = 100, message = AppConstants.MSG_NOMBRE_SIZE)
    private String nombre;

    @NotBlank(message = AppConstants.MSG_CODIGO_BLANK)
    @Pattern(regexp = AppConstants.CODIGO_PATTERN, message = AppConstants.MSG_CODIGO_PATTERN)
    private String codigo;

    private String descripcion;

    @NotNull(message = AppConstants.MSG_PRECIO_NULO)
    @Positive(message = AppConstants.MSG_PRECIO_POSITIVO)
    private BigDecimal precio;

    @NotNull(message = AppConstants.MSG_STOCK_NULO)
    @Min(value = 0, message = AppConstants.MSG_STOCK_MIN)
    private Integer stock;
}
