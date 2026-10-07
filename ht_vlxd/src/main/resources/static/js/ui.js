document.addEventListener('DOMContentLoaded', () => {
  const footer = document.querySelector('.workspace-footer'), main = document.querySelector('.main-content');
  if (footer && main) main.append(footer);
  document.querySelector('[data-sidebar-toggle]')?.addEventListener('click', event => { const open = document.body.classList.toggle('sidebar-open'); event.currentTarget.setAttribute('aria-expanded', String(open)); });
  document.querySelectorAll('.sidebar-link').forEach(link => { if (new URL(link.href).pathname === location.pathname) { link.classList.add('active'); link.setAttribute('aria-current', 'page'); } });
  document.querySelectorAll('[data-logout]').forEach(button => button.addEventListener('click', async () => {
    button.disabled = true;
    try { const response = await fetch('/api/logout', {method: 'POST'}); if (!response.ok) throw new Error('Không thể đăng xuất, vui lòng thử lại.'); ['logged_in_name', 'logged_in_username', 'logged_in_role', 'demo_role'].forEach(key => localStorage.removeItem(key)); location.href = '/'; }
    catch (error) { alert(error.message); button.disabled = false; }
  }));
  document.querySelectorAll('.form-group').forEach((group, i) => { const label = group.querySelector('label'), input = group.querySelector('input:not([type=hidden]),select,textarea'); if (label && input && !label.htmlFor) { if (!input.id) input.id = 'cmc-field-' + i; label.htmlFor = input.id; } });
  document.querySelectorAll('.table,.data-table').forEach(table => { if (!table.parentElement.classList.contains('table-scroll')) { const wrapper = document.createElement('div'); wrapper.className = 'table-scroll'; wrapper.style.overflowX = 'auto'; table.before(wrapper); wrapper.append(table); } });
  document.querySelectorAll('[data-account-role]').forEach(node => { localStorage.setItem('demo_role', node.dataset.accountRole); localStorage.setItem('logged_in_username', node.dataset.accountUsername); localStorage.setItem('logged_in_name', node.dataset.accountName); });
});
