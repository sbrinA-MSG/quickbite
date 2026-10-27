(function () {
  var cart = document.getElementById("cart");
  var form = document.getElementById("form");
  var receipt = document.getElementById("receipt");
  var submitBtn = document.getElementById("submit");
  var rupiah = new Intl.NumberFormat("id-ID", {style: "currency", currency: "IDR", maximumFractionDigits: 0});

  function el(tag, attrs, text) {
    var e = document.createElement(tag);
    for (var k in (attrs || {})) e.setAttribute(k, attrs[k]);
    if (text != null) e.textContent = text;
    return e;
  }

  var n = 0;
  function addRow(idMenu, qty) {
    n++;
    var row = el("div", {"class": "row"});
    var f1 = el("div", {"class": "field"});
    f1.appendChild(el("label", {"for": "menu" + n}, "ID menu"));
    var i1 = el("input", {id: "menu" + n, "data-role": "idMenu", value: idMenu || "", autocomplete: "off"});
    f1.appendChild(i1);
    var f2 = el("div", {"class": "field"});
    f2.appendChild(el("label", {"for": "qty" + n}, "Jumlah"));
    var i2 = el("input", {id: "qty" + n, "data-role": "qty", type: "number", min: "1", step: "1", value: qty || 1});
    f2.appendChild(i2);
    var rm = el("button", {type: "button", "class": "icon", "aria-label": "Hapus menu ini"}, "×");
    rm.addEventListener("click", function () {
      if (cart.children.length > 1) row.remove();
    });
    row.appendChild(f1); row.appendChild(f2); row.appendChild(rm);
    cart.appendChild(row);
  }

  function readCart() {
    return Array.prototype.map.call(cart.children, function (row) {
      return {
        idMenu: row.querySelector('[data-role="idMenu"]').value.trim(),
        qty: parseInt(row.querySelector('[data-role="qty"]').value, 10)
      };
    });
  }

  function validate(body) {
    var bad = [];
    if (!body.idPelanggan) bad.push("ID pelanggan");
    if (!body.tokenKartu) bad.push("Token kartu");
    body.keranjang.forEach(function (it, i) {
      if (!it.idMenu) bad.push("ID menu baris " + (i + 1));
      if (!(it.qty >= 1)) bad.push("Jumlah baris " + (i + 1));
    });
    return bad;
  }

  function show(nodes, fresh) {
    receipt.replaceChildren.apply(receipt, nodes);
    receipt.classList.remove("fresh");
    if (fresh) { void receipt.offsetWidth; receipt.classList.add("fresh"); }
  }

  function showError(title, detail) {
    var p = el("p", {"class": "msg", role: "alert"});
    p.appendChild(el("strong", null, title));
    p.appendChild(document.createTextNode(detail));
    show([el("h2", null, "STRUK"), p], true);
  }

  function showReceipt(data, body) {
    var dl = el("dl");
    function add(k, v) { dl.appendChild(el("dt", null, k)); dl.appendChild(el("dd", null, v)); }
    add("Pesanan", data.idPesanan);
    add("Pelanggan", body.idPelanggan);
    body.keranjang.forEach(function (it) { add(it.idMenu, "× " + it.qty); });
    var total = el("div", {"class": "total"});
    total.appendChild(el("span", null, "Total"));
    total.appendChild(el("span", null, rupiah.format(data.total)));
    show([
      el("h2", null, "STRUK"),
      el("p", {"class": "ok"}, "Pembayaran berhasil"),
      dl, el("hr"), total
    ], true);
  }

  // Terjemahan status HTTP -> pesan untuk pengguna
  var titles = {
    404: "Data tidak ditemukan",
    409: "Stok tidak cukup",
    502: "Pembayaran ditolak"
  };

  form.addEventListener("submit", function (ev) {
    ev.preventDefault();
    var body = {
      idPelanggan: document.getElementById("idPelanggan").value.trim(),
      tokenKartu: document.getElementById("tokenKartu").value.trim(),
      keranjang: readCart()
    };
    var bad = validate(body);
    if (bad.length) {
      showError("Data belum lengkap", "Periksa: " + bad.join(", ") + ".");
      return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = "Memproses…";
    fetch("/api/checkout", {
      method: "POST",
      headers: {"Content-Type": "application/json"},
      body: JSON.stringify(body)
    }).then(function (res) {
      return res.json().catch(function () { return {}; }).then(function (data) {
        if (res.ok) { showReceipt(data, body); return; }
        if (res.status === 400) {
          showError("Data tidak valid", "Server menolak isian. Pastikan semua kolom terisi dan jumlah minimal 1.");
        } else {
          showError(titles[res.status] || "Terjadi kesalahan (" + res.status + ")",
                    data.message || "Coba lagi beberapa saat.");
        }
      });
    }).catch(function () {
      showError("Server tidak terjangkau", "Pastikan aplikasi berjalan di port 8080, lalu coba lagi.");
    }).then(function () {
      submitBtn.disabled = false;
      submitBtn.textContent = "Bayar sekarang";
    });
  });

  document.getElementById("add").addEventListener("click", function () { addRow("", 1); });

  addRow("M-01", 2);
  addRow("M-02", 1);
})();
