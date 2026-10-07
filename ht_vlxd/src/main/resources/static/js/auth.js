(() => {
  const homes = {KHACH_HANG:'/san_pham',NV_KINH_DOANH:'/kinh_doanh/quan_ly_don_hang',NV_KE_TOAN:'/ke_toan/quan_ly_tai_chinh_va_cong_no',BAN_QUAN_LY:'/ban_quan_ly/dashboard_tong_quan',NV_KHO:'/quan_ly_kho/quan_ly_kho',QUAN_TRI_VIEN:'/quan_tri_vien/quan_ly_tai_khoan'};
  const message = document.getElementById('auth-message');
  function tab(name) { document.querySelectorAll('[data-auth-tab]').forEach(button => button.setAttribute('aria-selected', String(button.dataset.authTab === name))); document.getElementById('login-tab-content').hidden = name !== 'login'; document.getElementById('register-tab-content').hidden = name !== 'register'; message.hidden = true; }
  document.querySelectorAll('[data-auth-tab]').forEach(button => button.addEventListener('click', () => tab(button.dataset.authTab)));
  document.querySelectorAll('[data-auth-tab]').forEach(button => button.addEventListener('keydown', event => {
    const tabs = [...document.querySelectorAll('[data-auth-tab]')]; let next;
    if (event.key === 'ArrowRight') next = tabs[(tabs.indexOf(button) + 1) % tabs.length];
    if (event.key === 'ArrowLeft') next = tabs[(tabs.indexOf(button) + tabs.length - 1) % tabs.length];
    if (event.key === 'Home') next = tabs[0];
    if (event.key === 'End') next = tabs[tabs.length - 1];
    if (next) { event.preventDefault(); tab(next.dataset.authTab); next.focus(); }
  }));
  document.querySelectorAll('[data-password-toggle]').forEach(button => button.addEventListener('click', () => { const input = document.getElementById(button.dataset.passwordToggle); input.type = input.type === 'password' ? 'text' : 'password'; button.textContent = input.type === 'password' ? 'Hiện' : 'Ẩn'; button.setAttribute('aria-pressed', String(input.type === 'text')); }));
  async function send(url, payload) { const response = await fetch(url, {method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(payload)}); const raw = await response.text(); let data; try { data = JSON.parse(raw); } catch { data = raw; } if (!response.ok) throw new Error(data.message || data || 'Thao tác thất bại.'); return data; }
  function notice(text, success = false) { message.textContent = text; message.classList.toggle('success', success); message.hidden = false; }
  document.getElementById('login-form').addEventListener('submit', async event => { event.preventDefault(); const button = event.currentTarget.querySelector('[type=submit]'); button.disabled = true; message.hidden = true;
    try { const data = await send('/api/khach_hang/login', {username:document.getElementById('login-username').value.trim(),password:document.getElementById('login-password').value}); localStorage.setItem('demo_role', data.role); localStorage.setItem('logged_in_username', data.username); localStorage.setItem('logged_in_name', data.hoTen); location.href = data.role === 'KHACH_HANG' && new URLSearchParams(location.search).get('next') === 'cart' ? '/khach_hang/gio_hang' : homes[data.role] || '/'; }
    catch (error) { notice(error.message); button.disabled = false; }
  });
  document.getElementById('register-form').addEventListener('submit', async event => { event.preventDefault(); const password = document.getElementById('reg-password').value; if (password !== document.getElementById('reg-confirm').value) { notice('Mật khẩu xác nhận chưa trùng khớp.'); return; } const button = event.currentTarget.querySelector('[type=submit]'); button.disabled = true;
    try { const phone = document.getElementById('reg-phone').value.trim(); const result = await send('/api/khach_hang/register', {username:phone,fullname:document.getElementById('reg-fullname').value.trim(),phone,address:document.getElementById('reg-address').value.trim(),password}); tab('login'); document.getElementById('login-username').value = phone; notice(typeof result === 'string' ? result : 'Đã đăng ký tài khoản. Bạn có thể tiếp tục đăng nhập.', true); }
    catch (error) { notice(error.message); } finally { button.disabled = false; }
  });
  if (location.pathname === '/register') tab('register');
})();
