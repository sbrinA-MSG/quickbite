-- Data contoh untuk uji curl (P-01, M-01, M-02)
INSERT INTO pelanggan (id_pelanggan, nama, email, nomor_telepon, alamat)
VALUES ('P-01', 'Budi Santoso', 'budi@example.com', '081234567890', 'Malang');

INSERT INTO menu (id_menu, nama_menu, harga, stok) VALUES
    ('M-01', 'Nasi Goreng', 25000, 100),
    ('M-02', 'Es Teh',      25000, 100);
