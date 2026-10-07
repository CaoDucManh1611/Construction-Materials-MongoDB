// playwright-cli run-code --filename=scripts/motion_smoke.js
async page => {
  const base = 'http://127.0.0.1:8088';
  const errors = [];
  page.on('pageerror', error => errors.push(error.message));
  page.on('response', response => { if (response.url().startsWith(base) && response.status() >= 400) errors.push(response.status() + ' ' + response.url()); });
  const check = (ok, text) => { if (!ok) throw new Error(text); };
  await page.goto(base);
  await page.evaluate(() => localStorage.removeItem('cmc_motion_paused'));
  await page.emulateMedia({reducedMotion:'no-preference'});
  for (const width of [1440,768,375]) {
    await page.setViewportSize({width,height:900});
    await page.goto(base);
    check(await page.locator('body').evaluate(body => body.classList.contains('motion-enabled')), 'Motion not initialized');
    for (const section of ['#gioi-thieu','.material-editorial','.audience-section','#dich-vu','.supply-showcase','.faq-section','#quy-trinh']) {
      await page.locator(section).scrollIntoViewIfNeeded();
      check(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth + 1), 'Horizontal overflow: ' + width + ' ' + section);
    }
    const missing = await page.locator('main img').evaluateAll(images => images.filter(img => !img.complete || !img.naturalWidth).map(img => img.src));
    check(!missing.length, 'Missing images: ' + missing.join(', '));
    await page.locator('.faq-list summary').first().click();
    check(await page.locator('.faq-list details').first().getAttribute('open') !== null, 'FAQ did not open');
    await page.locator('.faq-list summary').first().click();
    await page.locator('[data-motion-toggle]').click();
    check(await page.locator('body').evaluate(body => body.classList.contains('motion-paused')), 'Pause failed');
    check(await page.locator('.marquee-track').evaluate(node => getComputedStyle(node).animationPlayState === 'paused'), 'Marquee still running');
    await page.reload();
    check(await page.locator('[data-motion-toggle]').getAttribute('aria-pressed') === 'true', 'Pause preference lost');
    await page.locator('[data-motion-toggle]').click();
    await page.locator('#gioi-thieu').scrollIntoViewIfNeeded();
    await page.waitForFunction(() => document.querySelector('.story-art').classList.contains('is-visible') && getComputedStyle(document.querySelector('.story-art')).opacity === '1');
    await page.screenshot({path:'.runtime/story-' + width + '.png',animations:'disabled'});
  }
  await page.emulateMedia({reducedMotion:'reduce'});
  await page.goto(base + '/#gioi-thieu');
  check(await page.locator('.story-art').evaluate(node => getComputedStyle(node).opacity === '1'), 'Reduced motion hides content');
  check(await page.locator('.marquee-track').evaluate(node => getComputedStyle(node).animationName === 'none'), 'Reduced motion still animating');
  await page.emulateMedia({reducedMotion:'no-preference'});
  await page.setViewportSize({width:1440,height:1000});
  await page.goto(base);
  check(await page.locator('.marquee-track').evaluate(node => getComputedStyle(node).animationName === 'marquee-flow'), 'Marquee animation missing');
  await page.mouse.move(1100,400);
  check(await page.locator('.hero-visual').evaluate(node => !!node.style.getPropertyValue('--rx')), 'Pointer tilt failed');
  await page.locator('[data-motion-toggle]').click();
  await page.screenshot({path:'.runtime/experience-full.png',fullPage:true,animations:'disabled'});
  await page.locator('[data-motion-toggle]').click();
  if (errors.length) throw new Error(errors.join('\n'));
  return 'Motion, pause persistence, reduced motion, pointer tilt, FAQ, local images and responsive layout passed.';
}
