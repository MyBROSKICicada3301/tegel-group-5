// auth-scripts.js - A single file to include all authentication-related scripts
// This file is meant to be included in all pages that need authentication functionality

// Create debug utility if it doesn't exist
if (typeof debug === 'undefined') {
    // Create minimal debug utility that won't cause errors even if the main debug-utils.js fails to load
    const debug = {
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
    window.debug = debug;
}

// Check if user is logged in and update the auth buttons accordingly
document.addEventListener('DOMContentLoaded', function() {
    console.log('Auth-scripts loaded, checking login status');

    // Function to update auth buttons
    function updateAuthButtons(isLoggedIn) {
        const authButtons = document.getElementById('authButtons');
        if (!authButtons) {
            console.warn('Auth buttons container not found on this page');
            return;
        }

        if (isLoggedIn) {
            authButtons.innerHTML = `
                <a href="account.html" class="btn btn-light me-2">Account</a>
                <a href="#" class="btn btn-light" id="signoutBtn">Sign Out</a>
            `;

            // Add click event for sign out button
            document.getElementById('signoutBtn').addEventListener('click', function(e) {
                e.preventDefault();
                console.log('Sign out button clicked');
                signOut();
            });
        } else {
            authButtons.innerHTML = `
                <a href="signup.html" class="btn btn-light me-2">Sign Up</a>
                <a href="login.html" class="btn btn-light">Login</a>
            `;
        }
    }

    // Function to check login status
    function checkLoginStatus() {
        console.log('Checking login status...');
        fetch('/tegel_webapp/check-session', {
            method: 'GET',
            credentials: 'include'
        })
        .then(response => {
            if (!response.ok) {
                console.error(`Session check failed with status: ${response.status}`);
                throw new Error(`Session check failed with status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Login status response:', data);
            updateAuthButtons(data.loggedIn);
        })
        .catch(error => {
            console.error('Error checking login status:', error);
            // Assume not logged in if there's an error
            updateAuthButtons(false);
        });
    }

    // Function to sign out
    function signOut() {
        console.log('Signing out...');
        fetch('/tegel_webapp/logout', {
            method: 'POST',
            credentials: 'include'
        })
        .then(response => {
            if (response.ok) {
                console.log('Signed out successfully');
                // Clear any stored user data
                localStorage.removeItem('userId');
                localStorage.removeItem('userLoggedIn');
                // Redirect to home page
                window.location.href = 'index.html';
            } else {
                console.error('Sign out failed:', response.status);
                alert('Failed to sign out. Please try again.');
            }
        })
        .catch(error => {
            console.error('Error during sign out:', error);
            alert('An error occurred while signing out.');
        });
    }

    // Expose sign out function to global scope
    window.signOut = signOut;

    // Check login status on page load
    checkLoginStatus();
});
