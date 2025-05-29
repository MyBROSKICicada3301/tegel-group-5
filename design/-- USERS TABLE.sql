-- USERS TABLE
CREATE TABLE Users (
    user_ID SERIAL PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    passwordHash TEXT NOT NULL,
    firstName VARCHAR(50),
    lastName VARCHAR(50),
    phoneNumber VARCHAR(20),
    dateOfBirth DATE,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    DietRes TEXT,
    role VARCHAR(50) NOT NULL
);

-- ANNOUNCEMENTS
CREATE TABLE Announcement (
    announcement_ID SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    content TEXT,
    postedBy INT,
    postedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    isPublic BOOLEAN,
    FOREIGN KEY (postedBy) REFERENCES Users(user_ID)
);

-- EVENTS
CREATE TABLE Event (
    event_ID SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    date DATE,
    location VARCHAR(100),
    image TEXT,
    maxParticipants INTEGER,
    CreatedBy INT,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    isActive BOOLEAN,
    FOREIGN KEY (CreatedBy) REFERENCES Users(user_ID)
);

-- EVENT REGISTRATIONS
CREATE TABLE EventRegistration (
    registration_ID SERIAL PRIMARY KEY,
    event_ID INT NOT NULL,
    user_ID INT NOT NULL,
    registrationData TEXT,
    status VARCHAR(50),
    specialRequests TEXT,
    FOREIGN KEY (event_ID) REFERENCES Event(event_ID),
    FOREIGN KEY (user_ID) REFERENCES Users(user_ID)
);

-- NEWSLETTERS
CREATE TABLE NewsLetter (
    letter_ID SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    content TEXT,
    postedBy INT,
    postedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (postedBy) REFERENCES Users(user_ID)
);

-- TASKS
CREATE TABLE Task (
    task_ID SERIAL PRIMARY KEY,
    assignedTo INT NOT NULL,
    relatedEvent INT,
    dueDate DATE,
    status VARCHAR(50),
    CreatedBy INT,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assignedTo) REFERENCES Users(user_ID),
    FOREIGN KEY (relatedEvent) REFERENCES Event(event_ID),
    FOREIGN KEY (CreatedBy) REFERENCES Users(user_ID)
);
