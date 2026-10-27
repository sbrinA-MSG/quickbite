package quickbite.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit test perilaku domain: tanpa Spring, tanpa database. */
class EntityDomainTest {

    @Test
    void menu_validasiStok_dan_kurangiStok() {
        Menu m = new Menu("M-01", "Nasi Goreng", 25000, 3);

        assertThat(m.validasiStok(3)).isTrue();
        assertThat(m.validasiStok(4)).isFalse();
        assertThat(m.validasiStok(0)).isFalse();
        assertThat(m.validasiStok(-1)).isFalse();

        assertThat(m.kurangiStok(2)).isTrue();
        assertThat(m.getStok()).isEqualTo(1);
        assertThat(m.kurangiStok(2)).isFalse();
        assertThat(m.getStok()).isEqualTo(1);
    }

    @Test
    void pesanan_hitungTotal_menjumlahkan_subtotal_item() {
        Pelanggan p = new Pelanggan("P-01", "Budi", "b@x.com", "0812", "Malang");
        Pesanan pesanan = new Pesanan("O-1", LocalDateTime.now(), p);
        pesanan.tambahItem(new ItemPesanan("I-1", 2, new Menu("M-01", "A", 10000, 9)));
        pesanan.tambahItem(new ItemPesanan("I-2", 1, new Menu("M-02", "B", 5000, 9)));

        assertThat(pesanan.hitungTotal()).isEqualTo(25000.0);
        assertThat(pesanan.getTotalBiaya()).isEqualTo(25000.0);
        assertThat(pesanan.getStatus()).isEqualTo(StatusPesanan.MENUNGGU_BAYAR);

        pesanan.updateStatus(StatusPesanan.DIPROSES);
        assertThat(pesanan.getStatus()).isEqualTo(StatusPesanan.DIPROSES);
    }

    @Test
    void pembayaran_prosesVerifikasi() {
        Pelanggan p = new Pelanggan("P-01", "Budi", "b@x.com", "0812", "Malang");
        Pesanan o = new Pesanan("O-1", LocalDateTime.now(), p);

        assertThat(new Pembayaran("B-1", o, 1, "MIDTRANS", "SUKSES", "REF").prosesVerifikasi()).isTrue();
        assertThat(new Pembayaran("B-2", o, 1, "MIDTRANS", "sukses", "REF").prosesVerifikasi()).isTrue();
        assertThat(new Pembayaran("B-3", o, 1, "MIDTRANS", "GAGAL", "REF").prosesVerifikasi()).isFalse();
        assertThat(new Pembayaran("B-4", o, 1, "MIDTRANS", "SUKSES", null).prosesVerifikasi()).isFalse();
    }

    @Test
    void pelanggan_info_dan_ubahAlamat() {
        Pelanggan p = new Pelanggan("P-01", "Budi", "b@x.com", "0812", "Malang");
        assertThat(p.getInfoPelanggan()).isEqualTo("Budi <b@x.com>");
        p.ubahAlamat("Surabaya");
        assertThat(p.getAlamat()).isEqualTo("Surabaya");
    }
}
