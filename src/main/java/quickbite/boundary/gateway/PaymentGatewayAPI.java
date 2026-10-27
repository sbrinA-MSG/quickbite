package quickbite.boundary.gateway;

/** Boundary eksternal (outbound): isolasi SDK/HTTP gateway di balik interface. */
public interface PaymentGatewayAPI {
    String mintaTransaksiPembayaran(double total, String tokenKartu);
}
