// Verificăm dacă utilizatorul este logat și returnăm token-ul
function checkLogin() {
    console.log('🔍 Verificăm login...');
    try {
        const token = localStorage.getItem('jwtToken');
        console.log('🔑 Token:', token ? token.substring(0, 20) + '...' : 'NULL');

        if (!token) {
            console.warn('⚠️ Token lipsește, redirectăm la login');
            window.location.href = '/login.html';
            return null;
        }
        return token;
    } catch (error) {
        console.error('❌ Eroare la citirea localStorage:', error);
        return null;
    }
}

// Fetch cu JWT și tratament erori
async function fetchWithAuth(url, options = {}) {
    console.log('📡 fetchWithAuth apelat pentru:', url);

    const token = checkLogin();
    if (!token) {
        console.error('❌ Token invalid, opresc request-ul');
        return;
    }

    const headers = {
        'Authorization': 'Bearer ' + token,
        'Content-Type': 'application/json',
        ...options.headers
    };

    console.log('📤 Trimit request cu headers:', headers);

    try {
        const response = await fetch(url, { ...options, headers });
        console.log('📥 Response status:', response.status);

        if (response.status === 401 || response.status === 403) {
            console.error('🚫 Acces interzis:', response.status);
            alert('❌ Nu ești autentificat sau token-ul a expirat.');
            logout();
            return;
        }

        const data = await response.json();
        console.log('✅ Date primite:', data);
        return data;

    } catch (error) {
        console.error('💥 Eroare în fetchWithAuth:', error);
        throw error;
    }
}

// Logout
function logout() {
    console.log('👋 Logout...');
    localStorage.clear();
    window.location.href = '/login.html';
}