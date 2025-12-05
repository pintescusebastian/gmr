document.getElementById('loginForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const doctorId = document.getElementById('doctorId').value.trim();
    const password = document.getElementById('password').value.trim();

    console.log('Trimit datele:', doctorId, password);

    try {
        const response = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include', // trimite cookie-uri
            body: JSON.stringify({ doctorCode: doctorId, password })
        });

        const data = await response.json();
        console.log('Răspuns backend:', data);

        if (data.token) {
            // Redirect direct fără alert
            window.location.href = '/dashboard.html';
        } else {
            alert('❌ Autentificare eșuată!'); // doar pentru eroare
        }

    } catch (error) {
        console.error('Eroare la autentificare:', error);
        alert('❌ Eroare la conectarea cu serverul.');
    }
});