# QuickBite - Checkout & Pembayaran (Spring Boot, BCE)

Implementasi Modul 11: Boundary - Controller - Entity.

## Struktur
- `boundary/`   : `CheckoutBoundary` (@RestController), `ApiExceptionHandler`, `dto/`, `gateway/` (PaymentGatewayAPI + Midtrans adapter)
- `controller/` : `CheckoutController` (@Service, @Transactional), `dto/` (Command/Result), `exception/`
- `entity/`     : Pelanggan, Pesanan, ItemPesanan, Menu, Pembayaran, StatusPesanan
- `repository/` : adapter persistensi (Spring Data JPA)

## Menjalankan
```bash
# 1. PostgreSQL
createdb quickbite   # user: quickbite
export DB_PASSWORD=...
# 2. (opsional) ganti gateway, mis. mock server lokal
export PAYMENT_BASE_URL=http://localhost:9090
# 3. Jalankan
mvn spring-boot:run
```
Flyway otomatis membuat skema (V1) dan data contoh P-01, M-01, M-02 (V2).

## Antarmuka (CheckoutUI)
Buka `http://localhost:8080/` setelah aplikasi berjalan. Halaman di `src/main/resources/static/` (`index.html`, `style.css`, `app.js`) memanggil `POST /api/checkout` dan menampilkan hasilnya sebagai struk.

## Uji
```bash
mvn test
curl -X POST http://localhost:8080/api/checkout \
  -H "Content-Type: application/json" \
  -d '{"idPelanggan":"P-01","tokenKartu":"tok_123",
       "keranjang":[{"idMenu":"M-01","qty":2},{"idMenu":"M-02","qty":1}]}'
```
Status: 200 sukses, 400 validasi, 404 data tidak ada, 409 stok habis, 502 gateway gagal.
