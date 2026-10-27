package quickbite.boundary;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import quickbite.controller.exception.PaymentFailedException;
import quickbite.controller.exception.ResourceNotFoundException;
import quickbite.controller.exception.StokTidakCukupException;

/** Menerjemahkan error domain -> HTTP. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(StokTidakCukupException.class)
    public ResponseEntity<ApiError> stokHabis(
            StokTidakCukupException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("STOK_HABIS", e.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> tidakDitemukan(
            ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ApiError> gagalBayar(
            PaymentFailedException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiError("PAYMENT_FAILED", e.getMessage()));
    }
}

record ApiError(String code, String message) { }
