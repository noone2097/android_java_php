<?php
// Prevent any output before our JSON response
ob_start();

// Error handling
error_reporting(E_ALL);
ini_set('display_errors', 0);
ini_set('log_errors', 1);
ini_set('error_log', __DIR__ . '/error.log');

// Database configuration
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "rent_hub_db";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    ob_end_clean();
    die(json_encode([
        'success' => false,
        'message' => 'Connection failed: ' . $conn->connect_error
    ]));
}

// Set charset to handle special characters
$conn->set_charset("utf8mb4");
?>
