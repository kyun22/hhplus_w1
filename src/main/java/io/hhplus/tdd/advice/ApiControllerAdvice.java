package io.hhplus.tdd.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.hhplus.tdd.dto.ErrorResponse;
import io.hhplus.tdd.exception.PointException;

@RestControllerAdvice
public class ApiControllerAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(500).body(new ErrorResponse("500", "에러가 발생했습니다."));
    }

    @ExceptionHandler(value = PointException.class)
    public ResponseEntity<ErrorResponse> handlePointException(PointException e) {
        return ResponseEntity.status(e.getErrorResult().getStatus())
          .body(new ErrorResponse(String.valueOf(e.getErrorResult().getStatus().value()), e.getErrorResult().getMessage()));
    }


}
