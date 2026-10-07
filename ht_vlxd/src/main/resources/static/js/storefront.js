(() => {
  function cart() { try { const data = JSON.parse(localStorage.getItem('shopping_cart') || '[]'); return Array.isArray(data) ? data : []; } catch { return []; } }
  function refreshCount() { const count = cart().reduce((n, item) => n + (Number(item.soLuong) || 0), 0); document.querySelectorAll('[data-cart-count]').forEach(node => { node.textContent = count; }); }
  function toast(message) { document.querySelector('.toast')?.remove(); const node = document.createElement('div'); node.className = 'toast'; node.setAttribute('role', 'status'); node.textContent = message; document.body.append(node); setTimeout(() => node.remove(), 4000); }
  document.querySelector('[data-store-toggle]')?.addEventListener('click', event => { const button = event.currentTarget; const open = document.getElementById('store-links').classList.toggle('is-open'); button.setAttribute('aria-expanded', String(open)); });
  document.querySelectorAll('[data-add-product]').forEach(button => button.addEventListener('click', () => {
    const item = button.dataset, input = document.getElementById('product-quantity'); const qty = input ? Number(input.value) : 1;
    if (!Number.isFinite(qty) || qty <= 0 || (input && !input.checkValidity())) { input?.reportValidity(); toast('Số lượng phải lớn hơn 0.'); return; }
    const items = cart(), existing = items.find(p => String(p.id) === item.id);
    if (existing) existing.soLuong = Number(existing.soLuong) + qty;
    else items.push({id: Number(item.id), tenHang: item.name, donGia: item.price, donViTinh: item.unit, anhUrl: item.image, quyCach: item.spec, soLuong: qty});
    localStorage.setItem('shopping_cart', JSON.stringify(items)); refreshCount(); toast('Đã thêm ' + item.name + ' vào giỏ hàng.');
  }));
  refreshCount(); window.addEventListener('storage', refreshCount);
  const search = document.getElementById('catalog-search'), sort = document.getElementById('catalog-sort'), grid = document.getElementById('catalog-grid');
  if (!grid) return;
  const cards = [...grid.querySelectorAll('.store-product')]; let category = new URLSearchParams(location.search).get('category') || 'all';
  const fold = text => text.normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/đ/g, 'd').toLowerCase();
  function filter() {
    const query = fold(search.value.trim()); let count = 0;
    cards.forEach(card => { const match = (category === 'all' || card.dataset.categoryId === category) && fold(card.dataset.name).includes(query); card.hidden = !match; if (match) count++; });
    document.getElementById('catalog-count').textContent = count + ' vật liệu'; document.getElementById('catalog-empty').hidden = count > 0;
    const sorted = [...cards].sort((a, b) => sort.value === 'low' ? Number(a.dataset.price) - Number(b.dataset.price) : sort.value === 'high' ? Number(b.dataset.price) - Number(a.dataset.price) : a.dataset.name.localeCompare(b.dataset.name, 'vi'));
    sorted.forEach(card => grid.append(card));
    document.querySelectorAll('[data-category]').forEach(button => { const active = button.dataset.category === category; button.classList.toggle('active', active); button.setAttribute('aria-pressed', String(active)); });
  }
  document.querySelectorAll('[data-category]').forEach(button => button.addEventListener('click', () => { category = button.dataset.category; filter(); })); search.addEventListener('input', filter); sort.addEventListener('change', filter); filter();
})();
