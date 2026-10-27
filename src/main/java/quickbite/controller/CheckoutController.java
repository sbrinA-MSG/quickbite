package quickbite.controller;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quickbite.boundary.gateway.PaymentGatewayAPI;
import quickbite.controller.dto.CheckoutCommand;
import quickbite.controller.dto.CheckoutResult;
import quickbite.controller.exception.PaymentFailedException;
import quickbite.controller.exception.ResourceNotFoundException;
import quickbite.controller.exception.StokTidakCukupException;
import quickbite.entity.ItemPesanan;
import quickbite.entity.Menu;
import quickbite.entity.Pelanggan;
import quickbite.entity.Pembayaran;
import quickbite.entity.Pesanan;
import quickbite.entity.StatusPesanan;
import quickbite.repository.MenuRepository;
import quickbite.repository.PelangganRepository;
import quickbite.repository.PesananRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * BCE Controller (application service): mengorkestrasi use case checkout.
 * Bukan @RestController - itu peran Boundary.
 */
@Service
public class CheckoutController {
    private final PelangganRepository pelangganRepo;
    private final MenuRepository menuRepo;
    private final PesananRepository pesananRepo;
    private final PaymentGatewayAPI paymentGateway;

    public CheckoutController(
            PelangganRepository pelangganRepo,
            MenuRepository menuRepo,
            PesananRepository pesananRepo,
            PaymentGatewayAPI paymentGateway) {
        this.pelangganRepo = pelangganRepo;
        this.menuRepo = menuRepo;
        this.pesananRepo = pesananRepo;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public CheckoutResult prosesCheckout(CheckoutCommand cmd) {
        // 1. Validasi pelanggan & stok (Menu dikunci pessimistik)
        Pelanggan pelanggan = pelangganRepo
                .findById(cmd.idPelanggan())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Pelanggan " + cmd.idPelanggan()));

        List<ResolvedItem> items = new ArrayList<>();
        for (CheckoutCommand.Item ci : cmd.items()) {
            Menu menu = menuRepo
                    .findByIdForUpdate(ci.idMenu())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Menu " + ci.idMenu()));
            if (!menu.validasiStok(ci.qty())) {
                throw new StokTidakCukupException(ci.idMenu());
            }
            items.add(new ResolvedItem(menu, ci.qty()));
        }

        // 2. Bentuk pesanan (komposisi Pesanan ◆ ItemPesanan)
        Pesanan pesanan = new Pesanan(
                UUID.randomUUID().toString(),
                LocalDateTime.now(), pelanggan);
        for (ResolvedItem ri : items) {
            pesanan.tambahItem(new ItemPesanan(
                    UUID.randomUUID().toString(),
                    ri.qty(), ri.menu()));
        }
        double total = pesanan.hitungTotal();

        // 3. Pembayaran lewat gateway (boundary eksternal)
        String kodeRef = paymentGateway
                .mintaTransaksiPembayaran(total, cmd.tokenKartu());

        Pembayaran pembayaran = new Pembayaran(
                UUID.randomUUID().toString(), pesanan,
                total, "MIDTRANS", "SUKSES", kodeRef);
        if (!pembayaran.prosesVerifikasi()) {
            throw new PaymentFailedException(
                    "Verifikasi pembayaran gagal");
        }
        pesanan.setPembayaran(pembayaran);

        // 4. Kurangi stok, ubah status, simpan
        for (ResolvedItem ri : items) {
            ri.menu().kurangiStok(ri.qty());
        }
        pesanan.updateStatus(StatusPesanan.DIPROSES);
        pesananRepo.save(pesanan);

        return new CheckoutResult(pesanan.getIdPesanan(), total);
    }

    private record ResolvedItem(Menu menu, int qty) { }
}
