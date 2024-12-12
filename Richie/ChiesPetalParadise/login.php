<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include_once 'config.php';

// Get raw POST data
$raw_input = file_get_contents('php://input');
error_log("Raw login input received: " . $raw_input);

$data = json_decode($raw_input);
error_log("Decoded login data: " . print_r($data, true));

if($data && isset($data->email) && isset($data->password)) {
    $email = $data->email;
    $password = $data->password;
    
    error_log("Attempting login for email: " . $email);
    
    try {
        $query = "SELECT * FROM users WHERE email = :email LIMIT 1";
        $stmt = $conn->prepare($query);
        $stmt->bindParam(":email", $email);
        $stmt->execute();
        
        error_log("Query executed, found rows: " . $stmt->rowCount());
        
        if($stmt->rowCount() > 0) {
            $row = $stmt->fetch(PDO::FETCH_ASSOC);
            error_log("User found, verifying password");
            
            if(password_verify($password, $row['password'])) {
                error_log("Password verified successfully");
                http_response_code(200);
                $response = array(
                    "status" => true,
                    "message" => "Login successful",
                    "user_id" => $row['id'],
                    "name" => $row['name']
                );
                echo json_encode($response);
                error_log("Login successful response: " . json_encode($response));
            } else {
                error_log("Password verification failed");
                http_response_code(401);
                echo json_encode(array(
                    "status" => false,
                    "message" => "Invalid password"
                ));
            }
        } else {
            error_log("No user found with email: " . $email);
            http_response_code(404);
            echo json_encode(array(
                "status" => false,
                "message" => "User not found"
            ));
        }
    } catch(PDOException $e) {
        error_log("Database error: " . $e->getMessage());
        http_response_code(500);
        echo json_encode(array(
            "status" => false,
            "message" => "Database error: " . $e->getMessage()
        ));
    }
} else {
    error_log("Incomplete login data. Raw input: " . $raw_input);
    http_response_code(400);
    echo json_encode(array(
        "status" => false,
        "message" => "Incomplete data"
    ));
}
?>
