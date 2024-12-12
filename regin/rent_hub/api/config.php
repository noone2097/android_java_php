<?php
$servername = "localhost";
$username = "root";  // Default Laragon MySQL username
$password = "";      // Default Laragon MySQL password
$dbname = "rent_hub_db";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die(json_encode([
        'success' => false,
        'message' => 'Connection failed: ' . $conn->connect_error
    ]));
}
?>
