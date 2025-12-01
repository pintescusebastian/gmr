// Verificăm dacă utilizatorul este logat și returnăm token-ul
function checkLogin() {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        window.location.href = 'login.html';
        return null;
    }
    return token;
}

// Fetch cu JWT și tratament erori
async function fetchWithAuth(url, options = {}) {
    const token = checkLogin();
    if (!token) return;

    const headers = {
        'Authorization': 'Bearer ' + token,
        'Content-Type': 'application/json',
        ...options.headers
    };

    const response = await fetch(url, { ...options, headers });

    if (response.status === 401 || response.status === 403) {
        alert('❌ Nu ești autentificat sau token-ul a expirat. Te rog loghează-te din nou.');
        logout();
        return;
    }

    return response.json();
}

// Logout
function logout() {
    localStorage.clear();
    window.location.href = 'login.html';
}
