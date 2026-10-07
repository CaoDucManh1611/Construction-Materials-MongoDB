(() => {
  const body = document.body;
  if (!body.classList.contains('experience')) return;
  const reduced = matchMedia('(prefers-reduced-motion: reduce)');
  const toggle = document.querySelector('[data-motion-toggle]');
  let paused = false;
  try { paused = localStorage.getItem('cmc_motion_paused') === 'true'; } catch {}
  const revealNodes = [...document.querySelectorAll('.section-head,.story-art,.story-copy,.category-card,.store-product,.bento-card,.audience-card,.steps>div,.supply-art,.supply-showcase>div:last-child,.faq-section>div,.cta-band')];
  let observer;
  function sync() {
    observer?.disconnect();
    const enabled = !paused && !reduced.matches;
    body.classList.toggle('motion-enabled', enabled);
    body.classList.toggle('motion-paused', paused || reduced.matches);
    toggle.setAttribute('aria-pressed', String(paused));
    toggle.textContent = paused ? 'Bật lại chuyển động' : 'Tạm dừng chuyển động';
    if (!enabled) return;
    observer = new IntersectionObserver(entries => entries.forEach(entry => {
      if (entry.isIntersecting) { entry.target.classList.add('is-visible'); observer.unobserve(entry.target); }
    }), {threshold:.08, rootMargin:'0px 0px -20px 0px'});
    revealNodes.forEach((node, i) => {
      node.dataset.reveal = '';
      node.style.setProperty('--reveal-delay', (i % 3) * 65 + 'ms');
      observer.observe(node);
    });
  }
  toggle.addEventListener('click', () => { paused = !paused; try { localStorage.setItem('cmc_motion_paused', String(paused)); } catch {} sync(); });
  reduced.addEventListener('change', sync);
  sync();
  const progress = document.querySelector('.reading-progress');
  let scheduled = false;
  function update() {
    scheduled = false;
    const range = document.documentElement.scrollHeight - innerHeight;
    progress.style.transform = 'scaleX(' + (range > 0 ? Math.min(1, scrollY / range) : 0) + ')';
  }
  addEventListener('scroll', () => { if (!scheduled) { scheduled = true; requestAnimationFrame(update); } }, {passive:true});
  addEventListener('resize', update);
  update();
  if (matchMedia('(hover:hover) and (pointer:fine)').matches) document.querySelectorAll('[data-tilt]').forEach(node => {
    node.addEventListener('pointermove', event => {
      if (paused || reduced.matches) return;
      const rect = node.getBoundingClientRect();
      node.style.setProperty('--rx', ((.5 - (event.clientY - rect.top) / rect.height) * 5) + 'deg');
      node.style.setProperty('--ry', (((event.clientX - rect.left) / rect.width - .5) * 5) + 'deg');
    });
    node.addEventListener('pointerleave', () => { node.style.setProperty('--rx','0deg'); node.style.setProperty('--ry','0deg'); });
  });
  document.addEventListener('visibilitychange', () => body.classList.toggle('motion-paused', document.hidden || paused || reduced.matches));
})();
