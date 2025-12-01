document.getElementById('loginForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const doctorId = document.getElementById('doctorId').value.trim();
    const password = document.getElementById('password').value.trim();

    console.log('Trimit datele:', doctorId, password); // debug

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ doctorCode: doctorId, password })
        });

        const data = await response.json();
        console.log('Răspuns backend:', data);

        if (data.token) {
            // salvăm JWT și informații despre doctor
            localStorage.setItem('jwtToken', data.token);
            localStorage.setItem('doctorName', doctorId);
            localStorage.setItem('isLoggedIn', 'true');

            window.location.href = 'dashboard.html';
        } else {
            alert('❌ Autentificare eșuată!');
        }

    } catch (error) {
        console.error('Eroare la autentificare:', error);
        alert('❌ Eroare la conectarea cu serverul.');
    }
});
