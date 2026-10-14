/**
 * app.js — MedScan DCMS Global Scripts
 * ─────────────────────────────────────
 * Small, focused utilities. No framework dependency.
 */

// ── CSRF helper for fetch() calls ────────────────────────────────────────────
function getCsrfToken() {
  const meta = document.querySelector('meta[name="_csrf"]');
  return meta ? meta.getAttribute('content') : '';
}
function getCsrfHeader() {
  const meta = document.querySelector('meta[name="_csrf_header"]');
  return meta ? meta.getAttribute('content') : 'X-CSRF-TOKEN';
}

// ── Auto-dismiss flash messages after 5 s ────────────────────────────────────
document.addEventListener('DOMContentLoaded', function () {
  const alerts = document.querySelectorAll('.alert-success-custom, .alert-error-custom');
  alerts.forEach(el => {
    setTimeout(() => {
      el.style.transition = 'opacity .5s';
      el.style.opacity = '0';
      setTimeout(() => el.remove(), 500);
    }, 5000);
  });
});

// ── Highlight active sidebar link (handled by Thymeleaf, but fallback) ───────
document.addEventListener('DOMContentLoaded', function () {
  const path = window.location.pathname.split('/')[1];
  document.querySelectorAll('.sidebar-link').forEach(link => {
    const href = link.getAttribute('href') || '';
    if (href.includes('/' + path) && path !== '') {
      link.classList.add('active');
    }
  });
});
