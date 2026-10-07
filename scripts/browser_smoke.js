// Run only against a fresh demo database with SEED_DEMO=true:
// playwright-cli open http://127.0.0.1:8088/login --browser=msedge
// playwright-cli run-code --filename=scripts/browser_smoke.js
async page => {
  const base = 'http://127.0.0.1:8088';
  const errors = [];
  page.on('pageerror', error => errors.push(error.message));
  page.on('response', response => { if (response.url().includes('/api/') && response.status() >= 400) errors.push(response.status() + ' ' + response.url()); });
  const roles = [
    ['giamdoc', '/ban_quan_ly/dashboard_tong_quan', 'Đề xuất danh mục chờ duyệt'],
    ['nvkho01', '/quan_ly_kho/quan_ly_kho', 'Lệnh xuất đã được kế toán phát hành'],
    ['nvkt01', '/ke_toan/quan_ly_tai_chinh_va_cong_no', 'Công nợ nhà cung cấp và hóa đơn'],
    ['khachhang01', '/khach_hang/don_hang_cua_toi', 'Báo giá và tư vấn'],
    ['nvkd01', '/kinh_doanh/quan_ly_don_hang', ''],
    ['admin', '/quan_tri_vien/quan_ly_tai_khoan', '']
  ];
  for (const [username, path, heading] of roles) {
    await page.goto(base + '/login');
    await page.getByRole('textbox', {name: 'Tên đăng nhập', exact: true}).fill(username);
    await page.locator('#login-password').fill('Admin@123');
    await page.locator('#login-tab-content').locator('[type=submit]').click();
    await page.waitForURL(url => url.pathname !== '/login');
    await page.goto(base + path);
    if (heading) await page.getByRole('heading', {name: heading, exact: true}).waitFor();
    const paths = {giamdoc:['/ban_quan_ly/bao_cao'], nvkho01:['/quan_ly_kho/giao_nhan_va_ban_giao','/quan_ly_kho/quan_ly_don_hang'], nvkt01:['/ke_toan/bao_cao','/ke_toan/quan_ly_nha_cung_cap'], nvkd01:['/kinh_doanh/quan_ly_hop_dong','/kinh_doanh/danh_muc_hang_hoa']};
    for (const route of [path, ...(paths[username] || []), '/dung_chung/thong_tin_ca_nhan']) {
      for (const width of [1440, 375]) {
        await page.setViewportSize({width,height:1000});
        const response = await page.goto(base + route);
        await page.waitForLoadState('networkidle');
        if (response.status() !== 200) throw new Error(route + ': HTTP ' + response.status());
        if (!(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth + 1))) throw new Error(route + ': overflow at ' + width);
        if (route === path && username === 'admin') {
          await page.locator('#btn-create-account').click();
          await page.locator('#createModal').waitFor({state:'visible'});
          if (!(await page.locator('#createModal').evaluate(node => node.getBoundingClientRect().width <= innerWidth))) throw new Error('Account modal overflow');
          await page.locator('#createModal').getByRole('button', {name:'Hủy bỏ',exact:true}).click();
        }
        if (route === path && width === 375) {
          await page.locator('[data-sidebar-toggle]').click();
          if (!(await page.locator('body').evaluate(body => body.classList.contains('sidebar-open')))) throw new Error('Mobile sidebar failed');
          await page.locator('[data-sidebar-toggle]').click();
        }
        if (route === path) await page.screenshot({path:'.runtime/' + username + '-' + width + '.png',fullPage:true,animations:'disabled'});
      }
    }
    if (await page.locator('body').innerText().then(text => /Whitelabel Error|HTTP Status 500/.test(text))) throw new Error('Server error on ' + path);
    console.log(username + ': page loaded');
  }
  if (errors.length) throw new Error(errors.join('\n'));
  return 'Six roles and fourteen operational screens passed at desktop/mobile widths without JavaScript exceptions.';
}
