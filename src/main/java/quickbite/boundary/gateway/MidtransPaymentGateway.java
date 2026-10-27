package quickbite.boundary.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import quickbite.controller.exception.PaymentFailedException;

import java.util.Map;

@Component
class MidtransPaymentGateway implements PaymentGatewayAPI {
    private final RestClient restClient;

    MidtransPaymentGateway(RestClient.Builder builder,
            @Value("${quickbite.payment.base-url}")
            String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public String mintaTransaksiPembayaran(
            double total, String token) {
        PaymentResponse res = restClient.post()
                .uri("/v1/transactions")
                .body(Map.of("total", total, "token", token))
                .retrieve()
                .body(PaymentResponse.class);
        if (res == null || !res.success()) {
            throw new PaymentFailedException(
                    "Gateway menolak transaksi");
        }
        return res.reference();
    }
}

record PaymentResponse(boolean success, String reference) { }
