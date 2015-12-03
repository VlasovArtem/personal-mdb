package com.vlasovartem.pmdb.utils.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.*;

/**
 * Created by artemvlasov on 01/12/15.
 */
@ControllerAdvice
public class SeriesRequestExceptionHandler {
    @ExceptionHandler
    public ResponseEntity seriesRequestExceptionHandler (Exception ex) {
        return ResponseEntity.status(FORBIDDEN).body(ex.getMessage());
    }
}
