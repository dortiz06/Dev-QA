package com.utch.microservicio.exception;

import com.utch.microservicio.util.AppConstants;

public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String id) {
        super(String.format(AppConstants.ERROR_PRODUCTO_NO_ENCONTRADO, id));
    }
}
