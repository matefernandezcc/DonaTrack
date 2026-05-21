// Mock auth — sessionStorage based. Demo only.
(function (global) {
  const KEY = 'donatrack_auth';

  function initLogin() {
    const form = document.getElementById('login-form');
    const alertBox = document.getElementById('login-alert');
    if (!form) return;

    if (sessionStorage.getItem(KEY) === '1') {
      window.location.href = 'dashboard.html';
      return;
    }

    form.addEventListener('submit', function (e) {
      e.preventDefault();
      const u = document.getElementById('username').value.trim();
      const p = document.getElementById('password').value;
      if (u === 'admin' && p === 'admin') {
        sessionStorage.setItem(KEY, '1');
        window.location.href = 'dashboard.html';
      } else {
        alertBox.hidden = false;
      }
    });
  }

  function guard() {
    if (sessionStorage.getItem(KEY) !== '1') {
      window.location.href = 'login.html';
    }
    document.addEventListener('click', function (e) {
      const t = e.target.closest('#logoutBtn');
      if (t) {
        e.preventDefault();
        sessionStorage.removeItem(KEY);
        window.location.href = 'login.html';
      }
    });
  }

  global.DT_Auth = { initLogin, guard };
})(window);
