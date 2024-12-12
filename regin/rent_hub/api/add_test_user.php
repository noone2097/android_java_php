<?php
include_once 'config.php';

// Create a test user if it doesn't exist
$username = "testuser";
$password = password_hash("testpass", PASSWORD_DEFAULT);

$sql = "INSERT INTO users (username, password) 
        SELECT * FROM (SELECT ?, ?) AS tmp 
        WHERE NOT EXISTS (
            SELECT username FROM users WHERE username = ?
        ) LIMIT 1";

$stmt = $conn->prepare($sql);
$stmt->bind_param("sss", $username, $password, $username);

if ($stmt->execute()) {
    if ($stmt->affected_rows > 0) {
        echo "Test user created successfully\n";
    } else {
        echo "Test user already exists\n";
    }
} else {
    echo "Error creating test user: " . $stmt->error . "\n";
}

$stmt->close();
$conn->close();
?>
