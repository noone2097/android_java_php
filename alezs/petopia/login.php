<?php
require_once 'config.php';
header('Content-Type: application/json');

// Enable error reporting
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Get JSON input
$json = file_get_contents('php://input');
$data = json_decode($json);

// Log input data
error_log("Login attempt - Input data: " . print_r($data, true));

$response = array('success' => false, 'message' => '');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $username = $data->username ?? '';
    $password = $data->password ?? '';

    if (empty($username) || empty($password)) {
        $response['message'] = 'Username and password are required';
        echo json_encode($response);
        exit;
    }

    try {
        // Log the SQL query parameters
        error_log("Attempting login for username: " . $username);
        
        $stmt = $conn->prepare("SELECT id, username, password FROM users WHERE username = ?");
        if (!$stmt) {
            throw new Exception("Prepare failed: " . $conn->error);
        }
        
        $stmt->bind_param("s", $username);
        if (!$stmt->execute()) {
            throw new Exception("Execute failed: " . $stmt->error);
        }
        
        $result = $stmt->get_result();
        $user = $result->fetch_assoc();

        // Log user data
        error_log("User data from DB: " . print_r($user, true));

        if ($user && password_verify($password, $user['password'])) {
            // Cast user_id to integer and verify it's valid
            $userId = (int)$user['id'];
            if ($userId <= 0) {
                throw new Exception("Invalid user ID: " . $userId);
            }

            $response['success'] = true;
            $response['message'] = 'Login successful';
            $response['user_id'] = $userId;
            $response['username'] = $user['username']; // Use username from DB
            
            // Log successful login
            error_log("Login successful - Response: " . print_r($response, true));
        } else {
            $response['message'] = 'Invalid username or password';
            error_log("Login failed - Invalid credentials for username: " . $username);
        }
        
        $stmt->close();
    } catch(Exception $e) {
        $response['success'] = false;
        $response['message'] = 'Server error: ' . $e->getMessage();
        error_log("Login error: " . $e->getMessage());
    }
} else {
    $response['message'] = 'Invalid request method';
    error_log("Invalid request method: " . $_SERVER['REQUEST_METHOD']);
}

// Log final response
error_log("Final response: " . json_encode($response));

echo json_encode($response);
?>
