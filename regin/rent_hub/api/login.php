<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");

include 'config.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"));
    
    if (!empty($data->username) && !empty($data->password)) {
        $username = mysqli_real_escape_string($conn, $data->username);
        
        $query = "SELECT id, username, password FROM users WHERE username = ?";
        $stmt = $conn->prepare($query);
        $stmt->bind_param("s", $username);
        $stmt->execute();
        $result = $stmt->get_result();
        
        if ($result->num_rows > 0) {
            $row = $result->fetch_assoc();
            if (password_verify($data->password, $row['password'])) {
                echo json_encode(array(
                    "success" => true,
                    "message" => "Login successful",
                    "user_id" => $row['id'],
                    "username" => $row['username']
                ));
            } else {
                echo json_encode(array("success" => false, "message" => "Invalid password"));
            }
        } else {
            echo json_encode(array("success" => false, "message" => "User not found"));
        }
    } else {
        echo json_encode(array("success" => false, "message" => "Username and password are required"));
    }
}
?>
