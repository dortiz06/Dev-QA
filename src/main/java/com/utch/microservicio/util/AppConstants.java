package com.utch.microservicio.util;

public final class AppConstants {

    public static final String COLECCION_PRODUCTOS = "productos";
    public static final String API_PRODUCTOS = "/api/productos";
    public static final String CODIGO_PATTERN = "^[A-Za-z0-9]+$";

    public static final String OPENAPI_TITLE = "Microservicio de Productos";
    public static final String OPENAPI_DESCRIPTION =
            "API REST para el alta, consulta, actualización y baja de productos.";
    public static final String OPENAPI_VERSION = "1.0.0";

    public static final String ERROR_VALIDACION = "Error de validación";
    public static final String ERROR_INTERNO = "Error interno del servidor";
    public static final String ERROR_CODIGO_DUPLICADO = "Ya existe un producto con el código indicado";
    public static final String ERROR_PRODUCTO_NO_ENCONTRADO = "No se encontró el producto con id: %s";

    public static final String MSG_NOMBRE_BLANK = "El nombre es obligatorio";
    public static final String MSG_NOMBRE_SIZE = "El nombre debe tener entre 3 y 100 caracteres";
    public static final String MSG_CODIGO_BLANK = "El código es obligatorio";
    public static final String MSG_CODIGO_PATTERN = "El código solo puede contener caracteres alfanuméricos";
    public static final String MSG_PRECIO_NULO = "El precio es obligatorio";
    public static final String MSG_PRECIO_POSITIVO = "El precio debe ser mayor a cero";
    public static final String MSG_STOCK_NULO = "El stock es obligatorio";
    public static final String MSG_STOCK_MIN = "El stock no puede ser negativo";

    private AppConstants() {
    }
}
