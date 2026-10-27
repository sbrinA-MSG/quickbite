package quickbite.boundary;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import quickbite.controller.CheckoutController;
import quickbite.controller.dto.CheckoutResult;
import quickbite.controller.exception.PaymentFailedException;
import quickbite.controller.exception.ResourceNotFoundException;
import quickbite.controller.exception.StokTidakCukupException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckoutBoundary.class)
class CheckoutBoundaryTest {

    private static final String BODY_VALID = """
            {"idPelanggan":"P-01",
             "tokenKartu":"tok_123",
             "keranjang":[
               {"idMenu":"M-01","qty":2}]}
            """;

    @Autowired MockMvc mvc;
    @MockBean CheckoutController checkoutController;

    @Test
    void checkout_berhasil_mengembalikan_id_dan_total() throws Exception {
        given(checkoutController.prosesCheckout(any()))
                .willReturn(new CheckoutResult("P-01", 50000.0));

        mvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPesanan").value("P-01"))
                .andExpect(jsonPath("$.total").value(50000.0));
    }

    @Test
    void checkout_stok_habis_mengembalikan_409() throws Exception {
        given(checkoutController.prosesCheckout(any()))
                .willThrow(new StokTidakCukupException("M-01"));

        mvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STOK_HABIS"));
    }

    @Test
    void checkout_data_tidak_ada_mengembalikan_404() throws Exception {
        given(checkoutController.prosesCheckout(any()))
                .willThrow(new ResourceNotFoundException("Pelanggan P-99"));

        mvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void checkout_gateway_gagal_mengembalikan_502() throws Exception {
        given(checkoutController.prosesCheckout(any()))
                .willThrow(new PaymentFailedException("Gateway menolak transaksi"));

        mvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALID))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("PAYMENT_FAILED"));
    }

    @Test
    void checkout_request_tidak_valid_mengembalikan_400() throws Exception {
        mvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idPelanggan":"",
                                 "tokenKartu":"tok_123",
                                 "keranjang":[]}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkout_qty_nol_mengembalikan_400() throws Exception {
        mvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idPelanggan":"P-01",
                                 "tokenKartu":"tok_123",
                                 "keranjang":[{"idMenu":"M-01","qty":0}]}
                                """))
                .andExpect(status().isBadRequest());
    }
}
