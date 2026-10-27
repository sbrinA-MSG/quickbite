package quickbite.boundary;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import quickbite.boundary.dto.CheckoutRequest;
import quickbite.boundary.dto.CheckoutResponse;
import quickbite.controller.CheckoutController;
import quickbite.controller.dto.CheckoutResult;

/**
 * BCE Boundary: hanya HTTP, validasi, dan konversi DTO -> Command.
 * Tidak ada logika bisnis di sini.
 */
@RestController
@RequestMapping("/api/checkout")
public class CheckoutBoundary {
    private final CheckoutController checkoutController;

    public CheckoutBoundary(
            CheckoutController checkoutController) {
        this.checkoutController = checkoutController;
    }

    @PostMapping
    public ResponseEntity<CheckoutResponse> checkout(
            @Valid @RequestBody CheckoutRequest request) {

        CheckoutResult hasil =
                checkoutController.prosesCheckout(
                        request.toCommand());
        return ResponseEntity.ok(new CheckoutResponse(
                hasil.idPesanan(), hasil.total()));
    }
}
