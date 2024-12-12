<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include_once 'config.php';

// Log the raw input
$raw_input = file_get_contents('php://input');
error_log("Raw input received: " . $raw_input);

// Get raw POST data
$data = json_decode($raw_input);

// Log the decoded data
error_log("Decoded data: " . print_r($data, true));

if($data && isset($data->name) && isset($data->email) && isset($data->password)) {
    try {
        error_log("Processing registration for email: " . $data->email);
        
        // Check if email already exists
        $check_query = "SELECT id FROM users WHERE email = :email LIMIT 1";
        $check_stmt = $conn->prepare($check_query);
        $check_stmt->bindParam(":email", $data->email);
        $check_stmt->execute();
        
        if($check_stmt->rowCount() > 0) {
            error_log("Email already exists: " . $data->email);
            http_response_code(400);
            echo json_encode(array(
                "status" => false,
                "message" => "Email already exists"
            ));
            exit();
        }
        
        // Hash password
        $hashed_password = password_hash($data->password, PASSWORD_DEFAULT);
        error_log("Password hashed successfully");
        
        // Insert new user
        $query = "INSERT INTO users (name, email, password) VALUES (:name, :email, :password)";
        $stmt = $conn->prepare($query);
        
        $stmt->bindParam(":name", $data->name);
        $stmt->bindParam(":email", $data->email);
        $stmt->bindParam(":password", $hashed_password);
        
        if($stmt->execute()) {
            $user_id = $conn->lastInsertId();
            error_log("User registered successfully with ID: " . $user_id);
            http_response_code(201);
            echo json_encode(array(
                "status" => true,
                "message" => "User registered successfully",
                "user_id" => $user_id
            ));
        } else {
            error_log("Failed to execute INSERT query");
            throw new Exception("Failed to insert user");
        }
    } catch(Exception $e) {
        error_log("Registration error: " . $e->getMessage());
        http_response_code(503);
        echo json_encode(array(
            "status" => false,
            "message" => "Unable to register user: " . $e->getMessage()
        ));
    }
} else {
    error_log("Incomplete data received. Raw input: " . $raw_input);
    http_response_code(400);
    echo json_encode(array(
        "status" => false,
        "message" => "Incomplete data"
    ));
}
?>
