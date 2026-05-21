/* ============================================================
   ui.js — Shared UI helpers (toast, modal, navbar, cart drawer)
   ============================================================ */

/* ══════════════════════════════════════════
   TOAST NOTIFICATIONS
══════════════════════════════════════════ */
const Toast = (() => {
  let container;
  function getContainer() {
    if (!container) {
      container = document.getElementById('toast-container');
      if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
      }
    }
    return container;
  }
  function show(message, type = 'success', duration = 3500) {
    const icons = { success: '✓', error: '✕', info: 'ℹ' };
    const el = document.createElement('div');
    el.className = `toast toast-${type}`;
    el.innerHTML = `<span>${icons[type] || '•'}</span><span>${message}</span>`;
    el.onclick = () => remove(el);
    getContainer().appendChild(el);
    setTimeout(() => remove(el), duration);
  }
  function remove(el) {
    el.classList.add('removing');
    setTimeout(() => el.remove(), 300);
  }
  return {
    success: (msg) => show(msg, 'success'),
    error:   (msg) => show(msg, 'error'),
    info:    (msg) => show(msg, 'info'),
  };
})();

/* ══════════════════════════════════════════
   MODAL
══════════════════════════════════════════ */
const Modal = {
  open(id)  { document.getElementById(id)?.classList.add('open'); },
  close(id) { document.getElementById(id)?.classList.remove('open'); },
  toggle(id){ document.getElementById(id)?.classList.toggle('open'); },
  closeOnOverlay(id) {
    const el = document.getElementById(id);
    if (el) el.addEventListener('click', e => { if (e.target === el) Modal.close(id); });
  },
};

/* ══════════════════════════════════════════
   FORMATTERS
══════════════════════════════════════════ */
const fmt = {
  currency: (v) => '₹' + parseFloat(v || 0).toFixed(2),
  date: (d) => d ? new Date(d).toLocaleString('en-IN', { day:'numeric', month:'short', year:'numeric', hour:'2-digit', minute:'2-digit' }) : '—',
  pct: (base, final) => {
    const b = parseFloat(base), f = parseFloat(final);
    if (!b || !f || f >= b) return 0;
    return Math.round(((b - f) / b) * 100);
  },
};

/* ══════════════════════════════════════════
   EMOJI BY CATEGORY
══════════════════════════════════════════ */
function categoryEmoji(cat = '') {
  const map = {
    grains:'🌾', rice:'🌾', wheat:'🌾', cereal:'🌾',
    dairy:'🥛', milk:'🥛', cheese:'🧀', butter:'🧈', curd:'🥛',
    vegetables:'🥦', veggies:'🥬', greens:'🥗',
    fruits:'🍎', fruit:'🍑',
    meat:'🥩', chicken:'🍗', fish:'🐟', seafood:'🦐',
    beverages:'🧃', drinks:'🧃', juice:'🍹', water:'💧',
    snacks:'🍿', chips:'🍟', biscuits:'🍪',
    bakery:'🍞', bread:'🥖',
    frozen:'🧊', eggs:'🥚',
    spices:'🌶️', masala:'🌶️', herbs:'🌿', salt:'🧂',
    oils:'🫙', ghee:'🫙',
    cleaning:'🧹', soap:'🧼',
    personal:'🧴', hygiene:'🧴',
    pulses:'🫘', lentils:'🫘', dal:'🫘',
    sugar:'🍬', sweets:'🍬',
  };
  const k = cat.toLowerCase();
  const match = Object.entries(map).find(([key]) => k.includes(key));
  return match ? match[1] : '🛒';
}

/* ══════════════════════════════════════════
   STATUS BADGE
══════════════════════════════════════════ */
function statusBadge(status) {
  const cfg = {
    PENDING:    'badge-orange',
    CONFIRMED:  'badge-green',
    PROCESSING: 'badge-blue',
    SHIPPED:    'badge-blue',
    DELIVERED:  'badge-green',
    CANCELLED:  'badge-red',
  };
  return `<span class="badge ${cfg[status] || 'badge-gray'}">${status}</span>`;
}

/* ══════════════════════════════════════════
   NAVBAR RENDERER
══════════════════════════════════════════ */
function renderNavbar(activePage) {
  const user = Auth.user;
  const isAdmin = Auth.isAdmin();
  const isCustomer = Auth.isCustomer();

  const links = isAdmin
    ? [{ href: '/pages/admin.html',   label: 'Dashboard' },
       { href: '/pages/admin.html#products', label: 'Products' },
       { href: '/pages/admin.html#orders',   label: 'Orders' }]
    : isCustomer
    ? [{ href: '/pages/shop.html',   label: 'Shop' },
       { href: '/pages/orders.html', label: 'My Orders' }]
    : [];

  const navLinksHtml = links.map(l =>
    `<a href="${l.href}" class="nav-link ${l.label === activePage ? 'active' : ''}">${l.label}</a>`
  ).join('');

  const rightHtml = user ? `
    <div class="user-pill">
      <div class="user-avatar">${(user.fullName||'U')[0].toUpperCase()}</div>
      <span class="user-name hide-mobile">${user.fullName?.split(' ')[0]}</span>
      <span class="user-role ${isAdmin ? 'role-admin' : 'role-customer'}">${isAdmin ? 'Admin' : 'Customer'}</span>
    </div>
    ${isCustomer ? `<button class="cart-btn" onclick="CartDrawer.open()">🛒 <span class="hide-mobile">Cart</span><span class="cart-badge" id="cart-badge" style="display:none">0</span></button>` : ''}
    <button class="logout-btn" onclick="handleLogout()">Sign Out</button>
  ` : `
    <a href="/pages/login.html" class="btn btn-ghost btn-sm">Sign In</a>
    <a href="/pages/register.html" class="btn btn-primary btn-sm">Register</a>
  `;

  const nav = document.getElementById('navbar');
  if (nav) nav.innerHTML = `
    <div class="nav-inner">
      <a href="${isAdmin ? '/pages/admin.html' : isCustomer ? '/pages/shop.html' : '/index.html'}" class="nav-brand">
        <span class="nav-logo">🌿</span>
        <span class="nav-name">Fresh<span>Mart</span></span>
      </a>
      <nav class="nav-links">${navLinksHtml}</nav>
      <div class="nav-right">${rightHtml}</div>
    </div>
  `;
}

function handleLogout() {
  Auth.clear();
  Toast.info('Signed out. See you soon!');
  setTimeout(() => window.location.href = '/pages/login.html', 600);
}

/* ══════════════════════════════════════════
   CART DRAWER
══════════════════════════════════════════ */
const CartDrawer = (() => {
  let items = [];

  function updateBadge() {
    const badge = document.getElementById('cart-badge');
    if (!badge) return;
    const total = items.reduce((s, i) => s + i.quantity, 0);
    badge.textContent = total;
    badge.style.display = total > 0 ? 'flex' : 'none';
  }

  function renderItems() {
    const container = document.getElementById('drawer-items');
    const footer    = document.getElementById('drawer-footer');
    if (!container) return;

    if (items.length === 0) {
      container.innerHTML = `
        <div class="empty-cart">
          <div class="empty-cart-icon">🛒</div>
          <div style="font-weight:700;font-size:16px">Your cart is empty</div>
          <p style="color:var(--text-muted);font-size:13px">Add products from the shop!</p>
        </div>`;
      if (footer) footer.style.display = 'none';
      return;
    }

    const total = items.reduce((s, i) => s + parseFloat(i.subtotal || 0), 0);
    container.innerHTML = items.map(item => {
      const hasImg = item.imageUrl && item.imageUrl.trim() !== '';
      const mediaHtml = hasImg
        ? `<img src="${item.imageUrl}" alt="${item.productName}"
            style="width:48px;height:48px;object-fit:cover;border-radius:8px;border:1px solid var(--border);flex-shrink:0"
            onerror="this.replaceWith(Object.assign(document.createElement('div'),{className:'cart-emoji',textContent:'${categoryEmoji(item.category)}'}))">`
        : `<div class="cart-emoji">${categoryEmoji(item.category)}</div>`;
      return `
      <div class="cart-item">
        ${mediaHtml}
        <div class="cart-info">
          <div class="cart-name">${item.productName}</div>
          <div class="cart-price">${fmt.currency(item.unitPrice)} each</div>
        </div>
        <div class="cart-right">
          <div class="qty-control">
            <button class="qty-btn" onclick="CartDrawer.changeQty(${item.cartItemId}, ${item.quantity - 1})" ${item.quantity <= 1 ? 'disabled' : ''}>−</button>
            <span class="qty-val">${item.quantity}</span>
            <button class="qty-btn" onclick="CartDrawer.changeQty(${item.cartItemId}, ${item.quantity + 1})">+</button>
          </div>
          <div class="cart-subtotal">${fmt.currency(item.subtotal)}</div>
          <button class="btn btn-danger btn-sm btn-icon" onclick="CartDrawer.removeItem(${item.cartItemId})" title="Remove">✕</button>
        </div>
      </div>
    `}).join('');

    if (footer) {
      footer.style.display = 'block';
      document.getElementById('drawer-total').textContent = fmt.currency(total);
    }
  }

  async function load() {
    if (!Auth.isCustomer()) return;
    try {
      const res = await API.cart.get();
      items = res?.data || [];
      renderItems();
      updateBadge();
    } catch {}
  }

  async function changeQty(itemId, qty) {
    if (qty <= 0) return removeItem(itemId);
    try {
      await API.cart.update(itemId, qty);
      await load();
    } catch (e) { Toast.error(e.message); }
  }

  async function removeItem(itemId) {
    try {
      await API.cart.remove(itemId);
      await load();
      Toast.info('Item removed');
    } catch (e) { Toast.error(e.message); }
  }

  function open() {
    document.getElementById('drawer-overlay')?.classList.add('open');
    document.getElementById('cart-drawer')?.classList.add('open');
    load();
  }
  function close() {
    document.getElementById('drawer-overlay')?.classList.remove('open');
    document.getElementById('cart-drawer')?.classList.remove('open');
  }

  return { open, close, load, changeQty, removeItem, items: () => items };
})();

/* ══════════════════════════════════════════
   CART DRAWER HTML — inject once per customer page
══════════════════════════════════════════ */
function injectCartDrawer() {
  if (!Auth.isCustomer()) return;
  if (document.getElementById('cart-drawer')) return;

  document.body.insertAdjacentHTML('beforeend', `
    <div class="drawer-overlay" id="drawer-overlay" onclick="CartDrawer.close()"></div>
    <div class="cart-drawer" id="cart-drawer">
      <div class="drawer-header">
        <span class="drawer-title">🛒 Your Cart</span>
        <button class="modal-close" onclick="CartDrawer.close()">✕</button>
      </div>
      <div class="drawer-items" id="drawer-items"></div>
      <div class="drawer-footer" id="drawer-footer" style="display:none">
        <div class="total-row">
          <span class="total-label">Estimated Total</span>
          <span class="total-amount" id="drawer-total">₹0.00</span>
        </div>
        <button class="btn btn-primary btn-xl btn-full" onclick="openCheckout()">Proceed to Checkout →</button>
      </div>
    </div>

    <!-- Checkout Modal -->
    <div class="modal-overlay" id="checkout-modal">
      <div class="modal">
        <div class="modal-header">
          <span class="modal-title">Confirm Order</span>
          <button class="modal-close" onclick="Modal.close('checkout-modal')">✕</button>
        </div>
        <div class="modal-body">
          <div id="checkout-summary"></div>
          <div class="form-group">
            <label class="form-label">Shipping Address *</label>
            <textarea class="form-textarea" id="shipping-address" rows="3" placeholder="Flat/House No, Street, City, State - PIN"></textarea>
          </div>
          <div id="checkout-alert"></div>
          <button class="btn btn-primary btn-xl btn-full" onclick="placeOrder()" id="place-order-btn">
            Confirm &amp; Place Order
          </button>
        </div>
      </div>
    </div>
    <div id="toast-container"></div>
  `);
  Modal.closeOnOverlay('checkout-modal');
}

function openCheckout() {
  const items = CartDrawer.items();
  if (!items.length) { Toast.error('Your cart is empty'); return; }
  const total = items.reduce((s, i) => s + parseFloat(i.subtotal || 0), 0);
  const summary = document.getElementById('checkout-summary');
  summary.innerHTML = `
    <div style="background:var(--gray-50);border:1px solid var(--border);border-radius:var(--radius-md);padding:14px;margin-bottom:16px">
      <div style="font-size:12px;font-weight:700;text-transform:uppercase;letter-spacing:0.5px;color:var(--text-muted);margin-bottom:10px">Order Summary</div>
      ${items.map(i => {
        const hasImg = i.imageUrl && i.imageUrl.trim() !== '';
        const thumb = hasImg
          ? `<img src="${i.imageUrl}" alt="${i.productName}" style="width:24px;height:24px;object-fit:cover;border-radius:4px;vertical-align:middle;margin-right:4px">`
          : categoryEmoji(i.category);
        return `
        <div style="display:flex;justify-content:space-between;font-size:13px;padding:4px 0;color:var(--text-light)">
          <span>${thumb} ${i.productName} × ${i.quantity}</span>
          <span style="font-weight:600">${fmt.currency(i.subtotal)}</span>
        </div>`;
      }).join('')}
      <div style="display:flex;justify-content:space-between;font-size:15px;font-weight:700;padding-top:10px;margin-top:6px;border-top:1px solid var(--border)">
        <span>Total</span><span style="color:var(--primary)">${fmt.currency(total)}</span>
      </div>
    </div>
  `;
  CartDrawer.close();
  Modal.open('checkout-modal');
}

async function placeOrder() {
  const address = document.getElementById('shipping-address').value.trim();
  const alertEl = document.getElementById('checkout-alert');
  const btn     = document.getElementById('place-order-btn');
  alertEl.innerHTML = '';
  if (!address) {
    alertEl.innerHTML = `<div class="alert alert-error">⚠ Please enter a shipping address.</div>`;
    return;
  }
  btn.disabled = true;
  btn.innerHTML = `<span class="spinner"></span> Placing order…`;
  try {
    await API.orders.place({ shippingAddress: address });
    Modal.close('checkout-modal');
    document.getElementById('shipping-address').value = '';
    Toast.success('🎉 Order placed successfully!');
    CartDrawer.load();
    if (typeof onOrderPlaced === 'function') onOrderPlaced();
  } catch (e) {
    alertEl.innerHTML = `<div class="alert alert-error">⚠ ${e.message}</div>`;
  } finally {
    btn.disabled = false;
    btn.innerHTML = 'Confirm &amp; Place Order';
  }
}
