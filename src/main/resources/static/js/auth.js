/* ============================================================
   auth.js — Session management and shared UI utilities
   Every page imports this file. It provides:
     - requireAuth()    : redirect to login if not logged in
     - requireRole()    : redirect if wrong role
     - getSession()     : returns cached user info
     - renderNavbar()   : builds the top nav for authenticated pages
     - toast()          : show a pop-up notification
     - formatDate()     : format ISO dates for display
     - statusBadge()    : render a colored badge for statuses
   ============================================================ */

// ── Session Storage ───────────────────────────────────────
// We store user info in sessionStorage so it survives page
// navigation within the same browser tab, but is cleared when
// the tab is closed. This is safer than localStorage for auth data.

function saveSession(user) {
  sessionStorage.setItem('rwm_user', JSON.stringify(user));
}

function getSession() {
  const raw = sessionStorage.getItem('rwm_user');
  return raw ? JSON.parse(raw) : null;
}

function clearSession() {
  sessionStorage.removeItem('rwm_user');
}

// ── Auth Guards ───────────────────────────────────────────
// Call requireAuth() at the top of every protected page.
// It checks the session and, if not logged in, asks the server.
// If the server also says "not logged in", redirect to login.
async function requireAuth() {
  let user = getSession();

  if (!user) {
    // No local session — try the server (user might have refreshed)
    const result = await Auth.me();
    if (!result.success) {
      // Definitely not logged in — send to login page
      window.location.href = 'index.html';
      return null;
    }
    user = result.data;
    saveSession(user);
  }

  return user;
}

// Call requireRole(['PASSAGER']) to restrict a page to one role.
// If the user has the wrong role, send them to their correct page.
async function requireRole(allowedRoles) {
  const user = await requireAuth();
  if (!user) return null;

  const role = user.role?.replace('ROLE_', '');
  if (!allowedRoles.includes(role)) {
    redirectToHome(role);
    return null;
  }

  return user;
}

// Redirect to the correct dashboard based on role
function redirectToHome(role) {
  const destinations = {
    PASSAGER:  'passenger.html',
    CHAUFFEUR: 'driver.html',
    ADMIN:     'admin.html'
  };
  window.location.href = destinations[role] || 'index.html';
}

// ── Navbar Builder ────────────────────────────────────────
// Inserts the sticky navbar into any page that calls this.
// 'activePage' highlights the current nav link.
function renderNavbar(user, activePage = '') {
  const role = user?.role?.replace('ROLE_', '') || '';
  const initials = user?.email?.[0]?.toUpperCase() || '?';

  // Build nav links based on role
  let links = '';
  if (role === 'PASSAGER') {
    links = `
      <button class="nav-link ${activePage==='browse'?'active':''}"
              onclick="location.href='browse.html'">Trajets</button>
      <button class="nav-link ${activePage==='dashboard'?'active':''}"
              onclick="location.href='passenger.html'">Mes réservations</button>
    `;
  } else if (role === 'CHAUFFEUR') {
    links = `
      <button class="nav-link ${activePage==='dashboard'?'active':''}"
              onclick="location.href='driver.html'">Tableau de bord</button>
    `;
  } else if (role === 'ADMIN') {
    links = `
      <button class="nav-link ${activePage==='dashboard'?'active':''}"
              onclick="location.href='admin.html'">Administration</button>
    `;
  }

  const navbar = document.createElement('nav');
  navbar.className = 'navbar';
  navbar.innerHTML = `
    <div class="navbar-brand">
      <span class="dot"></span>
      Ride <span>With Me</span>
    </div>
    <div class="navbar-links">
      ${links}
      <button class="nav-link ${activePage==='notifs'?'active':''}"
              onclick="location.href='notifications.html'">
        Notifications
        <span id="notif-badge" style="display:none"
              class="badge badge-amber" style="font-size:0.7rem; padding: 2px 7px;"></span>
      </button>
    </div>
    <div class="nav-user">
      <span style="font-size:0.8rem; color: var(--text-muted)">${user?.email || ''}</span>
      <div class="avatar">${initials}</div>
      <button class="btn btn-ghost btn-sm" id="logout-btn">Déconnexion</button>
    </div>
  `;

  document.body.prepend(navbar);

  // Wire logout button
  document.getElementById('logout-btn').addEventListener('click', async () => {
    await Auth.logout();
    clearSession();
    window.location.href = 'index.html';
  });

  // Load unread count for notification badge
  loadNotifBadge();
}

async function loadNotifBadge() {
  const result = await Notifications.nonLues();
  if (result.success && result.data?.length > 0) {
    const badge = document.getElementById('notif-badge');
    if (badge) {
      badge.textContent = result.data.length;
      badge.style.display = 'inline-flex';
    }
  }
}

// ── Toast Notifications ───────────────────────────────────
// Show a pop-up message at the bottom-right of the screen.
// type: 'success' | 'error' | 'info'
// duration: milliseconds before auto-dismiss (default 3500)
function toast(message, type = 'info', duration = 3500) {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }

  const icons = { success: '✓', error: '✕', info: 'ℹ' };
  const el = document.createElement('div');
  el.className = `toast toast-${type}`;
  el.innerHTML = `<span>${icons[type]}</span><span>${message}</span>`;
  container.appendChild(el);

  setTimeout(() => {
    el.style.animation = 'slideIn 0.2s ease reverse';
    setTimeout(() => el.remove(), 200);
  }, duration);
}

// ── Date Formatting ───────────────────────────────────────
function formatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('fr-FR', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });
}

function formatDateShort(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('fr-FR', {
    day: '2-digit', month: 'short',
    hour: '2-digit', minute: '2-digit'
  });
}

// ── Status Badges ─────────────────────────────────────────
// Returns an HTML string for a colored badge based on status value.
function statusBadge(status) {
  const map = {
    // Reservation statuses
    CONFIRMEE:    ['badge-green',  'Confirmée'],
    EN_ATTENTE:   ['badge-amber',  'En attente'],
    ANNULEE:      ['badge-red',    'Annulée'],
    TERMINEE:     ['badge-muted',  'Terminée'],
    // Trip statuses
    PREVU:        ['badge-blue',   'Prévu'],
    EN_COURS:     ['badge-green',  'En cours'],
    COMPLET:      ['badge-amber',  'Complet'],
    ANNULE:       ['badge-red',    'Annulé'],
    TERMINE:      ['badge-muted',  'Terminé'],
    // User statuses
    ACTIF:        ['badge-green',  'Actif'],
    SUSPENDU:     ['badge-amber',  'Suspendu'],
    BLOQUE:       ['badge-red',    'Bloqué'],
    // Payment statuses
    AUTORISE:     ['badge-blue',   'Autorisé'],
    CAPTURE:      ['badge-green',  'Capturé'],
    REMBOURSE:    ['badge-amber',  'Remboursé'],
  };
  const [cls, label] = map[status] || ['badge-muted', status || '—'];
  return `<span class="badge ${cls}">${label}</span>`;
}

// ── Role Badge ────────────────────────────────────────────
function roleBadge(role) {
  const map = {
    PASSAGER:  ['badge-blue',  'Passager'],
    CHAUFFEUR: ['badge-amber', 'Chauffeur'],
    ADMIN:     ['badge-red',   'Admin'],
  };
  const [cls, label] = map[role] || ['badge-muted', role];
  return `<span class="badge ${cls}">${label}</span>`;
}

// ── Loading State Helper ──────────────────────────────────
// Shows a spinner inside any container while data loads
function showLoading(containerId) {
  const el = document.getElementById(containerId);
  if (el) {
    el.innerHTML = `
      <div class="loading-overlay">
        <div class="spinner"></div>
        <span style="color: var(--text-muted); font-size: 0.875rem">Chargement...</span>
      </div>
    `;
  }
}

// ── Empty State Helper ────────────────────────────────────
function showEmpty(containerId, title, message, icon = '📭') {
  const el = document.getElementById(containerId);
  if (el) {
    el.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">${icon}</div>
        <h3>${title}</h3>
        <p>${message}</p>
      </div>
    `;
  }
}
