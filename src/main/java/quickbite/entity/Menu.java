package quickbite.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "menu")
public class Menu {
    @Id private String idMenu;
    private String namaMenu;
    private double harga;
    private int stok;

    protected Menu() { }

    public Menu(String idMenu, String namaMenu,
                double harga, int stok) {
        this.idMenu = idMenu;
        this.namaMenu = namaMenu;
        this.harga = harga;
        this.stok = stok;
    }

    public boolean validasiStok(int qty) {
        return qty > 0 && stok >= qty;
    }

    public boolean kurangiStok(int qty) {
        if (!validasiStok(qty)) return false;
        this.stok -= qty;
        return true;
    }

    public double getHarga() { return harga; }
    public String getIdMenu() { return idMenu; }
    public String getNamaMenu() { return namaMenu; }
    public int getStok() { return stok; }
}
