<?php
include_once 'config.php';

// First, let's make sure we have a test user
$username = "testuser";
$password = password_hash("testpass", PASSWORD_DEFAULT);

// Create test user if not exists
$sql = "INSERT IGNORE INTO users (username, password) VALUES (?, ?)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ss", $username, $password);
$stmt->execute();
$stmt->close();

// Get the user id (either the one just inserted or existing one)
$sql = "SELECT id FROM users WHERE username = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $username);
$stmt->execute();
$result = $stmt->get_result();
$user = $result->fetch_assoc();
$owner_id = $user['id'];
$stmt->close();

// Now insert a test property
$title = "Test Property";
$description = "This is a test property";
$price = 1000.00;
$location = "Test Location";
$image_url = "http://example.com/test.jpg";

$sql = "INSERT INTO properties (title, description, price, location, image_url, owner_id) 
        VALUES (?, ?, ?, ?, ?, ?)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ssdssi", $title, $description, $price, $location, $image_url, $owner_id);

if ($stmt->execute()) {
    echo "Success! Property inserted with ID: " . $conn->insert_id;
} else {
    echo "Error inserting property: " . $stmt->error;
}

$stmt->close();
$conn->close();
?>
