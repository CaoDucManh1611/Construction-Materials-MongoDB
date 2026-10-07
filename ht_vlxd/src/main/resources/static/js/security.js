(() => {
  window.escapeHtml = value => String(value ?? '').replace(/[&<>"']/g, character => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'})[character]);
  const originalFetch = window.fetch.bind(window);
  let token = '';
  const ready = originalFetch('/api/csrf', {credentials: 'same-origin'})
    .then(r => r.ok ? r.json() : Promise.reject(new Error('Không lấy được mã bảo vệ phiên.')))
    .then(data => { token = data.token; });
  window.fetch = async (input, options = {}) => {
    const method = (options.method || (input instanceof Request ? input.method : 'GET')).toUpperCase();
    const url = new URL(input instanceof Request ? input.url : input, location.href);
    if (url.origin === location.origin && !['GET', 'HEAD', 'OPTIONS'].includes(method)) {
      await ready;
      const headers = new Headers(options.headers || (input instanceof Request ? input.headers : undefined));
      headers.set('X-XSRF-TOKEN', token);
      options = {...options, headers};
    }
    return originalFetch(input, options);
  };
  document.addEventListener('click', async event => {
    const link = event.target.closest('a[onclick*="logoutUser"]');
    if (!link) return;
    event.preventDefault(); event.stopImmediatePropagation();
    try { await window.fetch('/api/logout', {method: 'POST'}); }
    finally { localStorage.removeItem('logged_in_name'); localStorage.removeItem('logged_in_username'); localStorage.removeItem('logged_in_role'); location.href = '/login'; }
  }, true);
  document.addEventListener('submit' , event => {
    const form = event.target;
    if (form.method.toUpperCase() === 'POST' && new URL(form.action).origin === location.origin) {
      let field = form.querySelector('input[name="_csrf"]');
      if (!field) { field = document.createElement('input'); field.type = 'hidden'; field.name = '_csrf'; form.append(field); }
      field.value = token;
      if (!token) event.preventDefault();
    }
  }, true);
})();
