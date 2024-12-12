# Rent Hub - Simple Property Listing App

A basic Android application for managing rental property listings.

## Features

### User Management
- Login/Register system
- Basic user authentication

### Property Management
- View list of rental properties
- Add new property listings
- Update property details
- Delete property listings

## Database Structure

### Users Table
```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE,
    password VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Properties Table
```sql
CREATE TABLE properties (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(100),
    address VARCHAR(255),
    price DECIMAL(10,2),
    description TEXT,
    owner_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id)
);
```

## API Endpoints

### User Management
- POST /login.php - User login
- POST /register.php - User registration

### Property Management
- GET /properties.php - List all properties
- POST /properties.php - Add new property
- PUT /properties.php - Update property
- DELETE /properties.php - Delete property
