///* ============================================================
//   api.js — Centralized API client with JWT injection
//   ============================================================ */
//
//const BASE = '/api';
//
///* ── Token helpers ── */
//const Auth = {
//  get token()   { return localStorage.getItem('token'); },
//  get user()    { try { return JSON.parse(localStorage.getItem('user')); } catch { return null; } },
//  set(data)     { localStorage.setItem('token', data.token); localStorage.setItem('user', JSON.stringify(data)); },
//  clear()       { localStorage.removeItem('token'); localStorage.removeItem('user'); },
//  isAdmin()     { return this.user?.role === 'ADMIN'; },
//  isCustomer()  { return this.user?.role === 'CUSTOMER'; },
//  isLoggedIn()  { return !!this.token; },
//};
//
///* ── Core fetch wrapper ── */
//async function request(method, path, body) {
//  const headers = { 'Content-Type': 'application/json' };
//  if (Auth.token) headers['Authorization'] = `Bearer ${Auth.token}`;
//
//  const res = await fetch(BASE + path, {
//    method,
//    headers,
//    body: body ? JSON.stringify(body) : undefined,
//  });
//
//  // 401 → logout and redirect
//  if (res.status === 401) {
//    Auth.clear();
//    window.location.href = '/pages/login.html';
//    return;
//  }
//
//  const data = await res.json().catch(() => ({}));
//
//  if (!res.ok) {
//    throw new Error(data.message || `HTTP ${res.status}`);
//  }
//  return data;
//}
//
//const get  = (path)        => request('GET',    path);
//const post = (path, body)  => request('POST',   path, body);
//const put  = (path, body)  => request('PUT',    path, body);
//const del  = (path)        => request('DELETE', path);
//const patch= (path, body)  => request('PATCH',  path, body);
//
///* ── API groups ── */
//const API = {
//
//  auth: {
//    login:    (data) => post('/auth/login',    data),
//    register: (data) => post('/auth/register', data),
//  },
//
//  products: {
//    getAll:       ()         => get('/products'),
//    getById:      (id)       => get(`/products/${id}`),
//    getByCategory:(cat)      => get(`/products/category/${encodeURIComponent(cat)}`),
//    search:       (q)        => get(`/products/search?keyword=${encodeURIComponent(q)}`),
//    // Admin
//    adminGetAll:  ()         => get('/admin/products'),
//    create:       (data)     => post('/admin/products', data),
//    update:       (id, data) => put(`/admin/products/${id}`, data),
//    delete:       (id)       => del(`/admin/products/${id}`),
//  },
//
//  cart: {
//    get:    ()               => get('/cart'),
//    add:    (productId, qty) => post('/cart', { productId, quantity: qty }),
//    update: (itemId, qty)    => put(`/cart/${itemId}?quantity=${qty}`),
//    remove: (itemId)         => del(`/cart/${itemId}`),
//    clear:  ()               => del('/cart'),
//  },
//
//  orders: {
//    place:       (data)        => post('/orders', data),
//    getMyOrders: ()            => get('/orders'),
//    getById:     (id)          => get(`/orders/${id}`),
//    // Admin
//    getAll:      ()            => get('/admin/orders'),
//    updateStatus:(id, status)  => patch(`/admin/orders/${id}/status?status=${status}`),
//  },
//};
//
///* ── Route guard ── */
//function requireAuth() {
//  if (!Auth.isLoggedIn()) {
//    window.location.href = '/pages/login.html';
//    return false;
//  }
//  return true;
//}
//function requireCustomer() {
//  if (!requireAuth()) return false;
//  if (!Auth.isCustomer()) { window.location.href = '/pages/admin.html'; return false; }
//  return true;
//}
//function requireAdmin() {
//  if (!requireAuth()) return false;
//  if (!Auth.isAdmin()) { window.location.href = '/pages/shop.html'; return false; }
//  return true;
//}

/* ============================================================
   api.js — Centralized API client with JWT injection
   ============================================================ */

const BASE = '/api';

/* ── Token helpers ── */
const Auth = {
  get token()   { return localStorage.getItem('token'); },
  get user()    { try { return JSON.parse(localStorage.getItem('user')); } catch { return null; } },
  set(data)     { localStorage.setItem('token', data.token); localStorage.setItem('user', JSON.stringify(data)); },
  clear()       { localStorage.removeItem('token'); localStorage.removeItem('user'); },
  isAdmin()     { return this.user?.role === 'ADMIN'; },
  isCustomer()  { return this.user?.role === 'CUSTOMER'; },
  isLoggedIn()  { return !!this.token; },
};

/* ── Core fetch wrapper ── */
async function request(method, path, body) {
  const headers = { 'Content-Type': 'application/json' };
  if (Auth.token) headers['Authorization'] = `Bearer ${Auth.token}`;

  const res = await fetch(BASE + path, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  // 401 → logout and redirect
  if (res.status === 401) {
    Auth.clear();
    window.location.href = '/pages/login.html';
    return;
  }

  const data = await res.json().catch(() => ({}));

  if (!res.ok) {
    throw new Error(data.message || `HTTP ${res.status}`);
  }
  return data;
}

const get  = (path)        => request('GET',    path);
const post = (path, body)  => request('POST',   path, body);
const put  = (path, body)  => request('PUT',    path, body);
const del  = (path)        => request('DELETE', path);
const patch= (path, body)  => request('PATCH',  path, body);

/* ── API groups ── */
const API = {

  auth: {
    login:    (data) => post('/auth/login',    data),
    register: (data) => post('/auth/register', data),
  },

  products: {
    getAll:       ()         => get('/products'),
    getById:      (id)       => get(`/products/${id}`),
    getByCategory:(cat)      => get(`/products/category/${encodeURIComponent(cat)}`),
    search:       (q)        => get(`/products/search?keyword=${encodeURIComponent(q)}`),
    // Admin
    adminGetAll:  ()         => get('/admin/products'),
    create:       (data)     => post('/admin/products', data),
    update:       (id, data) => put(`/admin/products/${id}`, data),
    delete:       (id)       => del(`/admin/products/${id}`),
  },

  cart: {
    get:    ()               => get('/cart'),
    add:    (productId, qty) => post('/cart', { productId, quantity: qty }),
    update: (itemId, qty)    => put(`/cart/${itemId}?quantity=${qty}`),
    remove: (itemId)         => del(`/cart/${itemId}`),
    clear:  ()               => del('/cart'),
  },

  orders: {
    place:       (data)        => post('/orders', data),
    getMyOrders: ()            => get('/orders'),
    getById:     (id)          => get(`/orders/${id}`),
    // Admin
    getAll:      ()            => get('/admin/orders'),
    updateStatus:(id, status)  => patch(`/admin/orders/${id}/status?status=${status}`),
  },

  // ── Analytics (new) ──────────────────────────────────────────────────────
  analytics: {
    getSummary: () => get('/admin/analytics/summary'),
  },
};

/* ── Route guard ── */
function requireAuth() {
  if (!Auth.isLoggedIn()) {
    window.location.href = '/pages/login.html';
    return false;
  }
  return true;
}
function requireCustomer() {
  if (!requireAuth()) return false;
  if (!Auth.isCustomer()) { window.location.href = '/pages/admin.html'; return false; }
  return true;
}
function requireAdmin() {
  if (!requireAuth()) return false;
  if (!Auth.isAdmin()) { window.location.href = '/pages/shop.html'; return false; }
  return true;
}
