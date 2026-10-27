package quickbite.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import quickbite.boundary.gateway.PaymentGatewayAPI;
import quickbite.controller.dto.CheckoutCommand;
import quickbite.controller.dto.CheckoutResult;
import quickbite.controller.exception.PaymentFailedException;
import quickbite.controller.exception.ResourceNotFoundException;
import quickbite.controller.exception.StokTidakCukupException;
import quickbite.entity.Menu;
import quickbite.entity.Pelanggan;
import quickbite.entity.Pesanan;
import quickbite.entity.StatusPesanan;
import quickbite.repository.MenuRepository;
import quickbite.repository.PelangganRepository;
import quickbite.repository.PesananRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock PelangganRepository pelangganRepo;
    @Mock MenuRepository menuRepo;
    @Mock PesananRepository pesananRepo;
    @Mock PaymentGatewayAPI gateway;

    CheckoutController controller;
    Pelanggan budi;
    Menu nasi;
    Menu teh;

    @BeforeEach
    void setUp() {
        controller = new CheckoutController(
                pelangganRepo, menuRepo, pesananRepo, gateway);
        budi = new Pelanggan("P-01", "Budi", "budi@example.com", "0812", "Malang");
        nasi = new Menu("M-01", "Nasi Goreng", 25000, 10);
        teh = new Menu("M-02", "Es Teh", 25000, 5);
    }

    private CheckoutCommand cmd(CheckoutCommand.Item... items) {
        return new CheckoutCommand("P-01", "tok_123", List.of(items));
    }

    @Test
    void checkout_berhasil_hitung_total_kurangi_stok_dan_simpan() {
        when(pelangganRepo.findById("P-01")).thenReturn(Optional.of(budi));
        when(menuRepo.findByIdForUpdate("M-01")).thenReturn(Optional.of(nasi));
        when(menuRepo.findByIdForUpdate("M-02")).thenReturn(Optional.of(teh));
        when(gateway.mintaTransaksiPembayaran(75000.0, "tok_123")).thenReturn("REF-1");

        CheckoutResult hasil = controller.prosesCheckout(cmd(
                new CheckoutCommand.Item("M-01", 2),
                new CheckoutCommand.Item("M-02", 1)));

        assertThat(hasil.total()).isEqualTo(75000.0);
        assertThat(nasi.getStok()).isEqualTo(8);
        assertThat(teh.getStok()).isEqualTo(4);

        ArgumentCaptor<Pesanan> captor = ArgumentCaptor.forClass(Pesanan.class);
        verify(pesananRepo).save(captor.capture());
        Pesanan tersimpan = captor.getValue();
        assertThat(tersimpan.getStatus()).isEqualTo(StatusPesanan.DIPROSES);
        assertThat(tersimpan.getItems()).hasSize(2);
        assertThat(tersimpan.getPembayaran().getKodeReferensi()).isEqualTo("REF-1");
        assertThat(tersimpan.getIdPesanan()).isEqualTo(hasil.idPesanan());
    }

    @Test
    void pelanggan_tidak_ada_melempar_ResourceNotFound() {
        when(pelangganRepo.findById("P-01")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.prosesCheckout(
                cmd(new CheckoutCommand.Item("M-01", 1))))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(gateway, never()).mintaTransaksiPembayaran(anyDouble(), anyString());
    }

    @Test
    void menu_tidak_ada_melempar_ResourceNotFound() {
        when(pelangganRepo.findById("P-01")).thenReturn(Optional.of(budi));
        when(menuRepo.findByIdForUpdate("M-99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.prosesCheckout(
                cmd(new CheckoutCommand.Item("M-99", 1))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void stok_kurang_melempar_exception_dan_tidak_memanggil_gateway() {
        when(pelangganRepo.findById("P-01")).thenReturn(Optional.of(budi));
        when(menuRepo.findByIdForUpdate("M-02")).thenReturn(Optional.of(teh));

        assertThatThrownBy(() -> controller.prosesCheckout(
                cmd(new CheckoutCommand.Item("M-02", 6))))
                .isInstanceOf(StokTidakCukupException.class);

        verify(gateway, never()).mintaTransaksiPembayaran(anyDouble(), anyString());
        verify(pesananRepo, never()).save(org.mockito.ArgumentMatchers.any());
        assertThat(teh.getStok()).isEqualTo(5);
    }

    @Test
    void gateway_gagal_tidak_mengurangi_stok_dan_tidak_menyimpan() {
        when(pelangganRepo.findById("P-01")).thenReturn(Optional.of(budi));
        when(menuRepo.findByIdForUpdate("M-01")).thenReturn(Optional.of(nasi));
        when(gateway.mintaTransaksiPembayaran(50000.0, "tok_123"))
                .thenThrow(new PaymentFailedException("Gateway menolak transaksi"));

        assertThatThrownBy(() -> controller.prosesCheckout(
                cmd(new CheckoutCommand.Item("M-01", 2))))
                .isInstanceOf(PaymentFailedException.class);

        assertThat(nasi.getStok()).isEqualTo(10);
        verify(pesananRepo, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
