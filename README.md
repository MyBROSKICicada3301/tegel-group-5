# TEGEL Group 5 Web Application

## Project Overview

This project is a web application for the TEGEL group, developed as part of a university assignment. The application provides features such as user registration, authentication, event management, announcements, newsletters, and administrative tools. 

_It is built using Java , HTML , CSS , JavaScript and a relational database backend._

## Features
- **User registration and login:**
  - New users can sign up with their email, password, and personal details. Passwords are securely hashed. Registered users can log in to access personalized features.
- **Profile management:**
  - Users can view and update their profile information, including name, contact details, and password. Profile changes are validated and securely stored.
- **Event listing, details, and registration:**
  - All users can browse a list of upcoming and past events. Each event has a detail page with description, date, time, location, and images. Logged-in users can register for events, and view their registered events.
- **Announcements and newsletters:**
  - Announcements are posted by admins and visible to all users on the homepage. Newsletters are managed by admins and can be viewed or subscribed to by users.
- **Admin panel for user and event management:**
  - Admins have access to a dedicated panel to manage users (view, edit, delete, reset passwords), create and edit events, post announcements, and manage newsletters.
- **Image upload and retrieval:**
  - Admins can upload images for events and announcements. Images are stored in the database or filesystem and displayed throughout the application.

## Technologies Used
- Java Servlet API
- JDBC (Java Database Connectivity)
- HTML, CSS, JavaScript (frontend)
- Maven (tool to build the project)
- GitLab Repository for version control

## Database
- PostgreSQL database hosted at: [bronto.ewi.utwente.nl](https://bronto.ewi.utwente.nl/phppgadmin/redirect.php?subject=root)

## Installation & Setup

### Prerequisites
- Java 17 or higher
- Apache Tomcat 10 or compatible servlet container
- Maven
- PostgreSQL database (see above)

### Steps
1. **Clone the repository:**
   ```sh
   git clone https://gitlab.utwente.nl/s3372871/tegel-group-5.git
   cd tegel-group-5
   ```
2. **Configure the database:**
   - Create a database and run the SQL scripts in `design/` to set up tables.
   - Update database credentials in `DatabaseManager.java` (found in `src/main/java/com/tegel/dao/`).
3. **Build the project:**
   ```sh
   mvn clean package
   ```
4. **Deploy the WAR file:**
   - Deploy `target/tegel-webapp-1.0-SNAPSHOT.war` to your servlet container (e.g., Tomcat's `webapps/` directory).
5. **Access the application:**
   - Open your browser and go to `http://localhost:8080/tegel-webapp/` (URL may vary based on deployment).

## Usage

Follow these steps to use the TEGEL Group 5 Web Application:

1. **Open the Application:**
   - In your web browser, navigate to `http://localhost:8080/tegel-webapp/` (or the appropriate deployment URL).

2. **Browse Events and Announcements:**
   - The homepage displays current announcements and a list of upcoming events.
   - Click on any event to view its details, including description, date, time, location, and images.

3. **Register for an Account:**
   - Click the "Sign Up" or "Register" button.
   - Fill in your email, password, and personal details.
   - Submit the form to create your account.
   - You can now log in using your credentials.

4. **Log In:**
   - Click the "Login" button.
   - Enter your registered email and password.
   - Upon successful login, you will have access to personalized features.

5. **Manage Your Profile:**
   - After logging in, go to your account/profile page.
   - View or update your personal information and change your password if needed.

6. **Register for Events:**
   - While logged in, browse the events list.
   - Click on an event and use the "Register" button to sign up for that event.
   - View your registered events in your profile or a dedicated section.

7. **View Newsletters:**
   - Access the newsletters section to read or subscribe to newsletters.

8. **Admin Features (for Admin Users):**
   - Log in with an admin account.
   - Access the admin panel to manage users, events, announcements, and newsletters.
   - Upload images for events and announcements as needed.

9. **Log Out:**
   - Click the "Logout" button to securely end your session.

## Development
- Source code is in `src/main/java/com/tegel/`.
- Frontend files are in `src/main/webapp/`.
- Tests are in `src/test/java/`.
- Use `mvn test` to run unit and integration tests.


## Authors
**TEGEL-5 team (_students of 2024-2025 batch BSc TCS at University of Twente_):**
- Ethan Noronha (s3361993)
- Cyril Shibu (s3412989)
- Gerben Bank (s3206726)
- Shishir Sudeesh Nambiar (s3372871)


## License
This project is for educational purposes.

## Acknowledgments
- University of Twente
- SGV TEGEL team
