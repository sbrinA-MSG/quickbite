(function () {
  // ---- Katalog tampilan. id & harga harus sama dengan tabel `menu` (V2__seed.sql).
  // `foto`: isi URL foto makanan untuk menggantikan ilustrasi bawaan.
  var CATALOG = [
    {
      id: "M-01", nama: "Nasi Goreng", harga: 25000, kat: "Makanan", art: "rice", foto: "",
      desk: "Nasi goreng hangat dengan telur, sayuran segar, dan kerupuk."
    },
    {
      id: "M-02", nama: "Es Teh", harga: 25000, kat: "Minuman", art: "tea", foto: "",
      desk: "Teh dingin yang segar, manis pas, penuh es batu."
    }
  ];

  var ART = {
    rice: '<svg viewBox="0 0 200 150" preserveAspectRatio="xMidYMid slice"><rect width="200" height="150" fill="#E6F4FB"/><ellipse cx="100" cy="112" rx="78" ry="20" fill="#A8D8F0"/>' +
      '<ellipse cx="100" cy="106" rx="68" ry="16" fill="#fff"/><path d="M44 100c4-34 28-52 56-52s52 18 56 52c-14 12-38 16-56 16s-42-4-56-16z" fill="#F3DDB4"/>' +
      '<g fill="#E8C98E"><circle cx="72" cy="84" r="4"/><circle cx="96" cy="72" r="4"/><circle cx="122" cy="82" r="4"/><circle cx="108" cy="98" r="4"/><circle cx="82" cy="102" r="4"/></g>' +
      '<g fill="#F7A8C4"><circle cx="88" cy="88" r="5"/><circle cx="116" cy="96" r="5"/></g><g fill="#8CCBA6"><circle cx="102" cy="82" r="3.5"/><circle cx="70" cy="94" r="3.5"/></g>' +
      '<circle cx="100" cy="64" r="15" fill="#fff"/><circle cx="100" cy="64" r="7" fill="#F6D98B"/></svg>',
    tea: '<svg viewBox="0 0 200 150" preserveAspectRatio="xMidYMid slice"><rect width="200" height="150" fill="#FCE3ED"/><ellipse cx="100" cy="132" rx="46" ry="8" fill="#F7A8C4" opacity=".6"/>' +
      '<path d="M66 40h68l-8 88a8 8 0 0 1-8 7H82a8 8 0 0 1-8-7z" fill="#fff" opacity=".9"/><path d="M70 62h60l-6 66a8 8 0 0 1-8 7H84a8 8 0 0 1-8-7z" fill="#D8A7B1"/>' +
      '<g fill="#fff" opacity=".8"><rect x="84" y="70" width="18" height="18" rx="4" transform="rotate(-12 93 79)"/><rect x="108" y="86" width="18" height="18" rx="4" transform="rotate(10 117 95)"/></g>' +
      '<circle cx="132" cy="46" r="16" fill="#F7E7A6"/><circle cx="132" cy="46" r="10" fill="none" stroke="#fff" stroke-width="2"/>' +
      '<path d="M104 44 120 10" stroke="#A8D8F0" stroke-width="6" stroke-linecap="round"/></svg>'
  };

  var $ = function (id) { return document.getElementById(id); };
  var rupiah = new Intl.NumberFormat("id-ID", { style: "currency", currency: "IDR", maximumFractionDigits: 0 });
  var cart = [];            // [{idMenu, qty}]
  var filter = { kat: "Semua", q: "" };
  var form = $("form"), submitBtn = $("submit"), receipt = $("receipt");

  function el(tag, attrs, text) {
    var e = document.createElement(tag);
    for (var k in (attrs || {})) e.setAttribute(k, attrs[k]);
    if (text != null) e.textContent = text;
    return e;
  }
  function icon(name) {
    var s = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    s.setAttribute("class", "ico");
    var u = document.createElementNS("http://www.w3.org/2000/svg", "use");
    u.setAttribute("href", "#i-" + name);
    s.appendChild(u);
    return s;
  }
  function find(id) { return CATALOG.filter(function (m) { return m.id === id; })[0]; }
  function visual(m, cls) {
    var box = el("div", { "class": cls });
    if (m && m.foto) {
      var img = el("img", { src: m.foto, alt: m.nama, loading: "lazy" });
      img.addEventListener("error", function () { img.remove(); box.insertAdjacentHTML("beforeend", ART[m.art]); });
      box.appendChild(img);
    } else if (m) {
      box.insertAdjacentHTML("beforeend", ART[m.art]);   // konstanta lokal, bukan input pengguna
    } else {
      box.textContent = "?";
    }
    return box;
  }

  // ---- Notifikasi
  function toast(text, kind) {
    var t = el("div", { "class": "toast" + (kind === "warn" ? " warn" : "") });
    t.appendChild(icon(kind === "warn" ? "alert" : "check"));
    t.appendChild(el("span", null, text));
    $("toasts").appendChild(t);
    setTimeout(function () { t.classList.add("out"); setTimeout(function () { t.remove(); }, 300); }, 2600);
  }

  // ---- Menu, kategori, pencarian
  function renderChips() {
    var box = $("chips");
    box.replaceChildren();
    ["Semua"].concat(CATALOG.map(function (m) { return m.kat; }).filter(function (k, i, a) { return a.indexOf(k) === i; }))
      .forEach(function (k) {
        var b = el("button", { type: "button", "class": "chip", "aria-pressed": String(filter.kat === k) }, k);
        b.addEventListener("click", function () { filter.kat = k; renderChips(); renderMenu(); });
        box.appendChild(b);
      });
  }
  function renderMenu() {
    var grid = $("menu"), q = filter.q.toLowerCase();
    grid.replaceChildren();
    var list = CATALOG.filter(function (m) {
      return (filter.kat === "Semua" || m.kat === filter.kat) &&
        (m.nama + " " + m.desk).toLowerCase().indexOf(q) !== -1;
    });
    if (!list.length) { grid.appendChild(el("p", { "class": "none" }, "Menu tidak ditemukan. Coba kata kunci atau kategori lain.")); return; }
    list.forEach(function (m) {
      var c = el("article", { "class": "card" });
      var ph = visual(m, "photo");
      ph.appendChild(el("span", { "class": "tag" }, m.kat));
      var b = el("div", { "class": "card-b" });
      b.appendChild(el("h3", null, m.nama));
      b.appendChild(el("p", null, m.desk));
      var f = el("div", { "class": "card-f" });
      f.appendChild(el("span", { "class": "price" }, rupiah.format(m.harga)));
      var add = el("button", { type: "button", "class": "add-btn", "aria-label": "Tambah " + m.nama + " ke keranjang" });
      add.appendChild(icon("plus")); add.appendChild(document.createTextNode("Tambah"));
      add.addEventListener("click", function () { addItem(m.id, 1); toast(m.nama + " masuk keranjang"); });
      f.appendChild(add);
      b.appendChild(f);
      c.appendChild(ph); c.appendChild(b);
      grid.appendChild(c);
    });
  }

  // ---- Keranjang
  function addItem(id, qty) {
    var it = cart.filter(function (c) { return c.idMenu === id; })[0];
    if (it) it.qty += qty; else cart.push({ idMenu: id, qty: qty });
    renderCart(true);
  }
  function totals() {
    var n = 0, sum = 0, unknown = false;
    cart.forEach(function (c) {
      n += c.qty;
      var m = find(c.idMenu);
      if (m) sum += m.harga * c.qty; else unknown = true;
    });
    return { n: n, sum: sum, unknown: unknown };
  }
  function renderCart(bump) {
    var box = $("cart");
    box.replaceChildren();
    if (!cart.length) box.appendChild(el("p", { "class": "empty-cart" }, "Keranjang masih kosong. Pilih menu favoritmu dulu."));
    cart.forEach(function (c) {
      var m = find(c.idMenu);
      var row = el("div", { "class": "line" });
      row.appendChild(visual(m, "thumb"));
      var info = el("div");
      info.appendChild(el("div", { "class": "line-name" }, m ? m.nama : c.idMenu));
      info.appendChild(el("div", { "class": "line-sub" }, m ? rupiah.format(m.harga * c.qty) : "Harga dihitung server"));
      row.appendChild(info);
      var step = el("div", { "class": "step" });
      var minus = el("button", { type: "button", "aria-label": c.qty > 1 ? "Kurangi jumlah" : "Hapus dari keranjang" });
      minus.appendChild(icon(c.qty > 1 ? "minus" : "trash"));
      minus.addEventListener("click", function () {
        c.qty--;
        if (c.qty < 1) cart.splice(cart.indexOf(c), 1);
        renderCart();
      });
      var qty = el("output", null, String(c.qty));
      var plus = el("button", { type: "button", "aria-label": "Tambah jumlah" });
      plus.appendChild(icon("plus"));
      plus.addEventListener("click", function () { c.qty++; renderCart(); });
      step.appendChild(minus); step.appendChild(qty); step.appendChild(plus);
      row.appendChild(step);
      box.appendChild(row);
    });
    var t = totals(), label = t.sum ? rupiah.format(t.sum) : "Rp0";
    $("count").textContent = t.n;
    $("mbarCount").textContent = t.n + " item";
    $("mbarTotal").textContent = label + (t.unknown ? "+" : "");
    var s = $("summary");
    s.replaceChildren();
    function sr(k, v, cls) { var d = el("div", cls ? { "class": cls } : null); d.appendChild(el("span", null, k)); d.appendChild(el("span", null, v)); s.appendChild(d); }
    sr("Jumlah item", String(t.n));
    sr(t.unknown ? "Total sementara" : "Total", label + (t.unknown ? "+" : ""), "sum-total");
    if (bump) { var c = $("count"); c.classList.remove("bump"); void c.offsetWidth; c.classList.add("bump"); }
  }

  // ---- Panel keranjang (bottom sheet di mobile)
  function openPanel(open) {
    $("panel").classList.toggle("open", open);
    $("backdrop").classList.toggle("show", open);
  }
  $("openCart").addEventListener("click", function () { openPanel(true); });
  $("mbar").addEventListener("click", function () { openPanel(true); });
  $("closeCart").addEventListener("click", function () { openPanel(false); });
  $("backdrop").addEventListener("click", function () { openPanel(false); });
  document.addEventListener("keydown", function (e) { if (e.key === "Escape") openPanel(false); });
  $("q").addEventListener("input", function (e) { filter.q = e.target.value.trim(); renderMenu(); });

  $("add").addEventListener("click", function () {
    var inp = $("manualId"), id = inp.value.trim();
    if (!id) { toast("Isi kode menu dulu", "warn"); return; }
    addItem(id, 1); inp.value = "";
  });

  // ---- Struk & pesan galat
  function show(nodes, fresh) {
    receipt.replaceChildren.apply(receipt, nodes);
    receipt.classList.remove("fresh");
    if (fresh) receipt.scrollIntoView({ block: "nearest", behavior: "smooth" });
  }
  function showError(title, detail) {
    var p = el("p", { "class": "msg", role: "alert" });
    p.appendChild(icon("alert"));
    var d = el("div");
    d.appendChild(el("strong", null, title));
    d.appendChild(document.createTextNode(detail));
    p.appendChild(d);
    show([el("h2", null, "Struk"), p], true);
    toast(title, "warn");
  }
  function showReceipt(data, body) {
    var dl = el("dl");
    function add(k, v) { dl.appendChild(el("dt", null, k)); dl.appendChild(el("dd", null, v)); }
    add("Pesanan", data.idPesanan);
    add("Pelanggan", body.idPelanggan);
    body.keranjang.forEach(function (it) { var m = find(it.idMenu); add(m ? m.nama : it.idMenu, "× " + it.qty); });
    var total = el("div", { "class": "total" });
    total.appendChild(el("span", null, "Total"));
    total.appendChild(el("span", null, rupiah.format(data.total)));
    var ok = el("p", { "class": "ok" });
    ok.appendChild(icon("check")); ok.appendChild(document.createTextNode("Pembayaran berhasil"));
    show([el("h2", null, "Struk"), ok, dl, el("hr"), total], true);
    toast("Pesanan " + data.idPesanan + " berhasil dibayar");
  }

  function validate(body) {
    var bad = [];
    if (!body.idPelanggan) bad.push("ID pelanggan");
    if (!body.tokenKartu) bad.push("Token kartu");
    if (!body.keranjang.length) bad.push("Keranjang");
    body.keranjang.forEach(function (it, i) {
      if (!it.idMenu) bad.push("ID menu baris " + (i + 1));
      if (!(it.qty >= 1)) bad.push("Jumlah baris " + (i + 1));
    });
    return bad;
  }

  // Terjemahan status HTTP -> pesan untuk pengguna
  var titles = { 404: "Data tidak ditemukan", 409: "Stok tidak cukup", 502: "Pembayaran ditolak" };

  form.addEventListener("submit", function (ev) {
    ev.preventDefault();
    var body = {
      idPelanggan: $("idPelanggan").value.trim(),
      tokenKartu: $("tokenKartu").value.trim(),
      keranjang: cart.map(function (c) { return { idMenu: c.idMenu, qty: c.qty }; })
    };
    var bad = validate(body);
    if (bad.length) { showError("Data belum lengkap", "Periksa: " + bad.join(", ") + "."); return; }

    submitBtn.disabled = true;
    submitBtn.textContent = "Memproses…";
    fetch("/api/checkout", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body)
    }).then(function (res) {
      return res.json().catch(function () { return {}; }).then(function (data) {
        if (res.ok) { showReceipt(data, body); cart = []; renderCart(); return; }
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

  renderChips();
  renderMenu();
  addItem("M-01", 2);
  addItem("M-02", 1);
})();