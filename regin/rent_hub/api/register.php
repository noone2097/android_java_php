<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");

include 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"));
    
    if (!empty($data->username) && !empty($data->email) && !empty($data->password)) {
        // Input validation
        if (!filter_var($data->email, FILTER_VALIDATE_EMAIL)) {
            echo json_encode(array("success" => false, "message" => "Invalid email format"));
            exit();
        }
        
        if (strlen($data->password) < 6) {
            echo json_encode(array("success" => false, "message" => "Password must be at least 6 characters"));
            exit();
        }
        
        $username = mysqli_real_escape_string($conn, $data->username);
        $email = mysqli_real_escape_string($conn, $data->email);
        $password = password_hash($data->password, PASSWORD_DEFAULT);
        
        $query = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        $stmt = $conn->prepare($query);
        $stmt->bind_param("sss", $username, $email, $password);
        
        if ($stmt->execute()) {
            echo json_encode(array("success" => true, "message" => "User registered successfully"));
        } else {
            echo json_encode(array("success" => false, "message" => "Registration failed"));
        }
    } else {
        echo json_encode(array("success" => false, "message" => "All fields are required"));
    }
}
?>
