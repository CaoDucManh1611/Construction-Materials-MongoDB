// Run on the isolated seeded demo, with playwright-cli run-code --filename=...
async page => {
  const base = 'http://127.0.0.1:8088';
  const errors = [];
  page.on('pageerror', error => errors.push(error.message));
  const check = (ok, message) => { if (!ok) throw new Error(message); };
  async function fits(path, width) {
    await page.setViewportSize({width, height:900});
    const response = await page.goto(base + path);
    check(response.status() === 200, path + ': HTTP ' + response.status());
    check(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth + 1), path + ': horizontal overflow at ' + width);
  }
  await page.context().clearCookies();
  await page.goto(base);
  await page.evaluate(() => localStorage.clear());
  for (const width of [1440, 768, 375]) {
    await fits('/', width);
    await page.getByRole('heading', {level:1}).waitFor();
    check(page.url() === base + '/', 'Guest was redirected from introduction');
    check(await page.evaluate(() => !localStorage.getItem('logged_in_username')), 'Guest received fake account');
    if (width === 375) {
      await page.locator('[data-store-toggle]').click();
      check(await page.locator('#store-links').isVisible(), 'Mobile navigation failed');
      await page.screenshot({path:'.runtime/home-mobile.png', fullPage:true,animations:'disabled'});
    }
    await fits('/san_pham', width);
    await fits('/login', width);
    if (width === 375) {
      await page.locator('#btn-login-tab').focus();
      await page.keyboard.press('ArrowRight');
      check(await page.locator('#register-tab-content').isVisible(), 'Keyboard registration tab failed');
      await page.keyboard.press('ArrowLeft');
      check(await page.locator('#login-tab-content').isVisible(), 'Keyboard login tab failed');
    }
  }
  await fits('/san_pham', 1440);
  check(await page.locator('.store-product').count() > 0, 'Catalog has no demo products');
  await page.locator('#catalog-search').fill('xi mang');
  check(await page.locator('.store-product:visible').count() > 0, 'Accent-insensitive search failed');
  await page.locator('#catalog-search').fill('no-such-material-123');
  check(await page.locator('#catalog-empty').isVisible(), 'Empty search feedback missing');
  await page.locator('#catalog-search').fill('');
  await page.locator('#catalog-sort').selectOption('low');
  const prices = await page.locator('.store-product').evaluateAll(cards => cards.map(card => Number(card.dataset.price)));
  check(prices.every((price, i) => !i || price >= prices[i-1]), 'Price sorting failed');
  await page.locator('[data-category]').nth(1).click();
  const category = await page.locator('[data-category]').nth(1).getAttribute('data-category');
  check(await page.locator('.store-product:visible').evaluateAll((cards, cat) => cards.every(card => card.dataset.categoryId === cat), category), 'Category filter failed');
  await page.locator('[data-category=all]').click();
  await page.locator('.product-art').first().click();
  await page.locator('#product-quantity').fill('0');
  await page.locator('[data-add-product]').click();
  check(await page.evaluate(() => !localStorage.getItem('shopping_cart')), 'Invalid quantity was added');
  await page.locator('#product-quantity').fill('2');
  await page.locator('[data-add-product]').click();
  check(await page.evaluate(() => JSON.parse(localStorage.getItem('shopping_cart'))[0].soLuong === 2), 'Cart quantity failed');
  await page.setViewportSize({width:375,height:900});
  check(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth + 1), 'Detail mobile overflow');
  await page.locator('.cart-link').click();
  check(page.url().includes('/login?next=cart'), 'Guest cart must request login');
  await page.locator('#login-username').fill('khachhang01');
  await page.locator('#login-password').fill('wrong-password');
  await page.locator('#login-form [type=submit]').click();
  await page.locator('#auth-message:not([hidden])').waitFor();
  check(page.url().includes('/login'), 'Invalid credentials redirected');
  await page.locator('#login-password').fill('Admin@123');
  await page.locator('#login-form [type=submit]').click();
  await page.waitForURL('**/khach_hang/gio_hang');
  check(await page.locator('body').innerText().then(text => !text.includes('Whitelabel')), 'Cart render failed');
  await page.screenshot({path:'.runtime/cart-mobile.png',fullPage:true,animations:'disabled'});
  await page.setViewportSize({width:1440,height:1000});
  await page.goto(base + '/');
  await page.screenshot({path:'.runtime/home-desktop.png',fullPage:true,animations:'disabled'});
  await page.goto(base + '/dung_chung/thong_tin_ca_nhan');
  await page.locator('.profile-menu summary').click();
  await page.locator('[data-logout]').click();
  await page.waitForURL(base + '/');
  check(await page.locator('.store-actions a[href="/login"]').isVisible(), 'Logout did not restore guest navigation');
  check(await page.evaluate(() => !localStorage.getItem('logged_in_username')), 'Logout retained account identity');
  if (errors.length) throw new Error(errors.join('\n'));
  return 'Public introduction, mobile navigation, catalog search/filter/sort, detail, guest cart and login passed.';
}
