// Mock auth — sessionStorage based. Demo only.
(function (global) {
  const KEY = 'donatrack_auth';

  function getUser() {
    try {
      const data = sessionStorage.getItem(KEY);
      return data ? JSON.parse(data) : null;
    } catch (e) {
      return null;
    }
  }

  function initLogin() {
    const form = document.getElementById('login-form');
    const alertBox = document.getElementById('login-alert');
    if (!form) return;

    const currentUser = getUser();
    if (currentUser) {
      window.location.href = 'dashboard.html?role=' + currentUser.role;
      return;
    }

    form.addEventListener('submit', function (e) {
      e.preventDefault();
      const u = document.getElementById('username').value.trim().toLowerCase();
      const p = document.getElementById('password').value;

      let role = null;
      let name = '';
      let initials = '';

      if (u === 'admin' && p === 'admin') {
        role = 'admin';
        name = 'Administrador';
        initials = 'AD';
      } else if (u === 'donante' && p === 'donante') {
        role = 'donante';
        name = 'María González';
        initials = 'MG';
      } else if (u === 'beneficiaria' && p === 'beneficiaria') {
        role = 'beneficiaria';
        name = 'Fundación Esperanza';
        initials = 'FE';
      }

      if (role) {
        const userSession = { username: u, role: role, name: name, initials: initials };
        sessionStorage.setItem(KEY, JSON.stringify(userSession));
        window.location.href = 'dashboard.html?role=' + role;
      } else {
        alertBox.hidden = false;
      }
    });
  }

  function guard() {
    const currentUser = getUser();
    if (!currentUser) {
      window.location.href = 'login.html';
      return;
    }

    // Double check that if url has a different role, we redirect or adjust
    const urlParams = new URLSearchParams(window.location.search);
    const roleParam = urlParams.get('role');
    if (roleParam && roleParam !== currentUser.role) {
      // Keep URL parameter in sync with session
      window.location.search = '?role=' + currentUser.role;
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

  global.DT_Auth = { initLogin, guard, getUser };
})(window);
