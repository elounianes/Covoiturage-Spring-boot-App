/* ============================================================
   api.js — All communication with the Spring Boot backend
   Every function returns a Promise that resolves to:
     { success: true,  data: ... }    on success
     { success: false, error: '...' } on failure
   This consistent shape means every caller handles errors
   the same way without parsing different response structures.
   ============================================================ */

// Base URL of your Spring Boot server.
// Change this if your backend runs on a different port.
const API_BASE = 'http://localhost:8081';

// ── Core fetch wrapper ────────────────────────────────────
// All API calls go through this function.
// It handles: JSON headers, credentials (session cookie),
// error parsing, and a consistent return shape.
async function apiCall(method, path, body = null) {
  const options = {
    method,
    // credentials: 'include' is critical — it tells the browser
    // to send the JSESSIONID cookie with every request so Spring
    // Security knows who is logged in.
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' }
  };

  // Only attach a body for POST/PUT/PATCH requests
  if (body && method !== 'GET') {
    options.body = JSON.stringify(body);
  }

  try {
    const res = await fetch(API_BASE + path, options);
    const json = await res.json();

    if (!res.ok) {
      // Backend returned 4xx/5xx — extract the error message
      // Our GlobalExceptionHandler always puts it in json.error
      return {
        success: false,
        error: json.error || json.message || 'Une erreur est survenue'
      };
    }

    // 2xx response — return the data directly
    return { success: true, data: json.data ?? json };

  } catch (err) {
    // Network error — backend is probably not running
    return {
      success: false,
      error: 'Impossible de contacter le serveur. Vérifiez que le backend tourne.'
    };
  }
}

// ── Convenience shortcuts ─────────────────────────────────
const get    = (path)         => apiCall('GET',    path);
const post   = (path, body)   => apiCall('POST',   path, body);
const put    = (path, body)   => apiCall('PUT',    path, body);
const del    = (path)         => apiCall('DELETE', path);

// ── Auth endpoints ────────────────────────────────────────
const Auth = {
  // Register a new account.
  // role must be 'PASSAGER', 'CHAUFFEUR', or 'ADMIN'
  register: (nom, prenom, email, phone, password, role) =>
    post('/api/auth/register', { nom, prenom, email, phone, password, role }),

  // Login uses form-data NOT JSON because Spring Security's
  // UsernamePasswordAuthenticationFilter expects form params.
  login: async (email, password) => {
    const form = new FormData();
    form.append('email', email);
    form.append('password', password);

    try {
      const res = await fetch(API_BASE + '/api/auth/login', {
        method: 'POST',
        credentials: 'include',
        body: form
        // No Content-Type header — browser sets multipart/form-data automatically
      });
      const json = await res.json();
      if (!res.ok) return { success: false, error: json.error || 'Identifiants incorrects' };
      return { success: true, data: json };
    } catch {
      return { success: false, error: 'Impossible de contacter le serveur' };
    }
  },

  // Logout — invalidates server session and clears cookie
  logout: () => post('/api/auth/logout'),

  // Returns the logged-in user's info, or null if not logged in
  me: () => get('/api/auth/me')
};

// ── Trajet (Trip) endpoints ───────────────────────────────
const Trajets = {
  // Public browse — no login required
  // Pass origine + destination to filter, or leave empty for all
  disponibles: (origine = '', destination = '') => {
    let path = '/api/trajets/disponibles';
    if (origine && destination) {
      path += `?origine=${encodeURIComponent(origine)}&destination=${encodeURIComponent(destination)}`;
    }
    return get(path);
  },

  // Get a single trip by ID
  byId: (id) => get(`/api/trajets/${id}`),

  // Driver: create a new trip
  creer: (data) => post('/api/trajets', data),

  // Driver: cancel/close their trip
  clore: (id) => del(`/api/trajets/${id}`),

  // Driver: get all their own trips
  mesTrajets: () => get('/api/chauffeur/trajets'),

  // Driver: get reservations on one of their trips
  reservationsDuTrajet: (trajetId) =>
    get(`/api/chauffeur/trajets/${trajetId}/reservations`),
};

// ── Reservation endpoints ─────────────────────────────────
const Reservations = {
  // Passenger: book a trip
  creer: (trajetId, nombrePlaces) =>
    post('/api/reservations', { trajetId, nombrePlaces }),

  // Passenger: see all their bookings
  mesReservations: () => get('/api/reservations/mes-reservations'),

  // Passenger: cancel a booking
  annuler: (id) => del(`/api/reservations/${id}`),

  // Driver: confirm a booking (triggers payment capture)
  confirmer: (id) => post(`/api/reservations/${id}/confirmer`),

  // Driver: cancel a confirmed reservation (triggers refund)
  annulerParChauffeur: (id) => del(`/api/reservations/${id}/chauffeur`),
};

// ── Chauffeur endpoints ───────────────────────────────────
const Chauffeur = {
  // List the driver's vehicles
  vehicules: () => get('/api/chauffeur/vehicules'),

  // Add a new vehicle
  ajouterVehicule: (data) => post('/api/chauffeur/vehicules', data),

  // Delete a vehicle
  supprimerVehicule: (id) => del(`/api/chauffeur/vehicules/${id}`),

  // Get the driver's average rating
  notes: () => get('/api/chauffeur/notes'),
};

// ── Notification endpoints ────────────────────────────────
const Notifications = {
  // All notifications for the logged-in user
  all: () => get('/api/notifications'),

  // Only unread ones
  nonLues: () => get('/api/notifications/non-lues'),

  // Mark one as read
  lire: (id) => put(`/api/notifications/${id}/lire`),
};
// Add to api.js

// Trip: mark as finished (driver action)
Trajets.terminer = (id) => post(`/api/trajets/${id}/terminer`);

// Evaluation
const Evaluations = {
  evaluerChauffeur: (chauffeurId, note) =>
    post(`/api/evaluations/chauffeur/${chauffeurId}`, { note })
};
Reservations.refuser = (id) => post(`/api/reservations/${id}/refuser`);
// Payment methods for passenger
const Paiement = {
  list:    ()   => get('/api/passager/moyens-paiement'),
  ajouter: (data) => post('/api/passager/moyens-paiement', data),
  supprimer: (id) => del(`/api/passager/moyens-paiement/${id}`)
};

// ── Admin endpoints ───────────────────────────────────────
const Admin = {
  users:          () => get('/api/admin/users'),
  trajets:        () => get('/api/admin/trajets'),
  reservations:   () => get('/api/admin/reservations'),
  suspendre:      (id) => put(`/api/admin/users/${id}/suspendre`),
  bloquer:        (id) => put(`/api/admin/users/${id}/bloquer`),
  creerAdmin:     (nom, prenom, email, phone, password) => 
    post('/api/admin/create-admin', { nom, prenom, email, phone, password, role: 'ADMIN' }),
};
