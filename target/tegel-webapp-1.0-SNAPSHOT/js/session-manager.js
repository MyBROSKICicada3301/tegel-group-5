// session-manager.js - Handles session management across all pages

// Create debug utility if it doesn't exist
if (typeof debug === 'undefined') {
    // Create minimal debug utility that won't cause errors even if the main debug-utils.js fails to load
    window.debug = {
        log: function(source, message, data) {
            console.log(`[${source}] ${message}`, data || '');
        },
        error: function(source, message, error) {
            console.error(`[${source}] ERROR: ${message}`, error || '');
        },
        warn: function(source, message, data) {
            console.warn(`[${source}] WARNING: ${message}`, data || '');
        },
        info: function(source, message, data) {
            console.info(`[${source}] INFO: ${message}`, data || '');
        }
    };
}

// Check if user is logged in when the page loads
document.addEventListener('DOMContentLoaded', function() {
    debug.log('SessionManager', 'DOM loaded, checking login status');
    checkLoginStatus();
});

// Function to check if user is logged in by checking for session
function checkLoginStatus() {
    debug.log('SessionManager', 'Checking login status...');

    // Log the URL being called for troubleshooting
    debug.info('SessionManager', 'Fetching session from: /tegel_webapp/check-session');

    fetch('/tegel_webapp/check-session', {
        method: 'GET',
        credentials: 'include'
    })
    .then(response => {
        if (!response.ok) {
            debug.error('SessionManager', `Session check failed with status: ${response.status}`);
            throw new Error(`Session check failed with status: ${response.status}`);
        }
        debug.log('SessionManager', 'Session response received, parsing JSON');
        return response.json();
    })
    .then(data => {
        debug.log('SessionManager', 'Login status response:', data);
        if(data.loggedIn) {
            debug.log('SessionManager', 'User is logged in with role:', data.role);

            // Special handling for admin users
            if (data.role === 'admin') {
                // Redirect admin to adminindex.html if on index.html
                if (window.location.pathname.endsWith('index.html')) {
                    debug.log('SessionManager', 'Admin detected on index.html, redirecting to adminindex.html');
                    window.location.href = 'adminindex.html';
                    return;
                }

                // Update all navigation links for admin users
                // 1. Update "Home" link in navbar
                const homeLinks = document.querySelectorAll('a.nav-link[href="index.html"]');
                homeLinks.forEach(link => {
                    debug.log('SessionManager', 'Updating Home link for admin');
                    link.setAttribute('href', 'adminindex.html');
                });

                // 2. Update Logo link in navbar
                const logoLinks = document.querySelectorAll('a.navbar-brand[href="index.html"]');
                logoLinks.forEach(link => {
                    debug.log('SessionManager', 'Updating Logo link for admin');
                    link.setAttribute('href', 'adminindex.html');
                });

                // 3. Also update any other index.html links that might be in the page
                const otherIndexLinks = document.querySelectorAll('a[href="index.html"]:not(.nav-link):not(.navbar-brand)');
                otherIndexLinks.forEach(link => {
                    debug.log('SessionManager', 'Updating other index link for admin');
                    link.setAttribute('href', 'adminindex.html');
                });

                // Make admin-only elements visible
                const adminOnlyElements = document.querySelectorAll('.admin-only');
                adminOnlyElements.forEach(element => {
                    element.style.display = 'block';
                });
            }

            // User is logged in, update navigation buttons
            const authButtons = document.getElementById('authButtons');
            if (authButtons) {
                debug.log('SessionManager', 'Updating auth buttons for logged in user');
                authButtons.innerHTML = `
                    <a href="#" class="btn btn-light me-2" id="accountBtn">Account</a>
                    <a href="#" class="btn btn-light" id="signoutBtn">Sign Out</a>
                `;

                // Add click event listener to sign out button
                document.getElementById('signoutBtn').addEventListener('click', function(e) {
                    e.preventDefault();
                    debug.log('SessionManager', 'Sign out button clicked');
                    signOut();
                });

                // Add click event for account button
                document.getElementById('accountBtn').addEventListener('click', function(e) {
                    e.preventDefault();
                    debug.log('SessionManager', 'Account button clicked, redirecting to account.html');
                    window.location.href = 'account.html';
                });
            }

            // Check if on admin pages and if user has admin role
            if (data.role === 'admin') {
                // Allow admin content access
                const adminOnlyElements = document.querySelectorAll('.admin-only');
                adminOnlyElements.forEach(element => {
                    element.style.display = 'block';
                });
            }
        } else {
            debug.log('SessionManager', 'User is not logged in');
            // User is not logged in
            // If trying to access protected pages, redirect to login
            if (isProtectedPage()) {
                debug.log('SessionManager', 'Redirecting to login from protected page');
                window.location.href = 'login.html';
            }
        }
    })
    .catch(error => {
        debug.error('SessionManager', 'Error checking session status:', error);
    });
}

// Function to determine if current page is protected (requiring login)
function isProtectedPage() {
    // Add paths of pages that require authentication
    const protectedPaths = [
        '/admin',
        '/admin.html',
        '/adminindex.html',
        '/admin-create.html',
        '/account.html'
    ];

    const currentPath = window.location.pathname;
    const basePathWithoutContext = currentPath.substring(currentPath.lastIndexOf('/'));

    return protectedPaths.some(path =>
        currentPath.endsWith(path) || basePathWithoutContext === path
    );
}

// Function to sign out
function signOut() {
    fetch('/tegel_webapp/logout', {
        method: 'POST',
        credentials: 'include'
    })
    .then(() => {
        // Redirect to home page after successful logout
        window.location.href = 'index.html';
    })
    .catch(error => {
        debug.error('SessionManager', 'Error signing out:', error);
    });
}

// Keep session alive by making periodic requests to the server
function keepSessionAlive() {
    // Make a request every minute to keep the session active
    setInterval(() => {
        fetch('/tegel_webapp/check-session', {
            method: 'GET',
            credentials: 'include'
        }).catch(error => {
            debug.error('SessionManager', 'Error keeping session alive:', error);
        });
    }, 60000); // 60,000 ms = 1 minute
}

// Start the session keep-alive mechanism
if (document.cookie.includes('JSESSIONID')) {
    keepSessionAlive();
}
