(function () {
  function init() {
    var sidebar = document.querySelector('.admin-sidebar');
    var layout = document.querySelector('.admin-layout');
    if (!sidebar || !layout) return;

    var bar = document.createElement('div');
    bar.className = 'admin-topbar';
    bar.innerHTML = '<button type="button" class="admin-menu-btn" aria-label="Open menu" aria-expanded="false">' +
                    '<i class="fas fa-bars"></i></button><span>Sarvasva Admin</span>';
    var overlay = document.createElement('div');
    overlay.className = 'admin-overlay';

    layout.parentNode.insertBefore(bar, layout);
    document.body.appendChild(overlay);

    var btn = bar.querySelector('.admin-menu-btn');
    function setOpen(open) {
      document.body.classList.toggle('admin-menu-open', open);
      btn.setAttribute('aria-expanded', open ? 'true' : 'false');
    }
    btn.addEventListener('click', function () {
      setOpen(!document.body.classList.contains('admin-menu-open'));
    });
    overlay.addEventListener('click', function () { setOpen(false); });
    sidebar.addEventListener('click', function (e) {
      if (e.target.closest('a')) setOpen(false);
    });
    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape') setOpen(false);
    });
    window.addEventListener('resize', function () {
      if (window.innerWidth > 768) setOpen(false);
    });
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init);
  else init();
})();
