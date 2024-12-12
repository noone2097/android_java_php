<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, GET, PUT, DELETE");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

// Enable error reporting for debugging
ini_set('display_errors', 1);
error_reporting(E_ALL);

require_once 'config.php';

// Initialize response array
$response = array('success' => false, 'message' => '', 'pets' => array());

try {
    // Get JSON input
    $json = file_get_contents('php://input');
    if (!$json) {
        throw new Exception("No input data received");
    }

    // Log received data
    error_log("Received data in pets.php: " . $json);

    // Decode JSON
    $data = json_decode($json);
    if ($data === null) {
        throw new Exception("Invalid JSON data: " . json_last_error_msg());
    }

    // Check for user_id
    if (!isset($data->user_id)) {
        throw new Exception("User ID is required");
    }

    // Validate user_id
    $user_id = (int)$data->user_id;
    if ($user_id <= 0) {
        throw new Exception("Invalid user ID");
    }

    // Prepare and execute query
    $sql = "SELECT id, user_id, name, type, age, price, description, image_url, created_at 
            FROM pets 
            WHERE user_id = ?
            ORDER BY created_at DESC";
            
    $stmt = $conn->prepare($sql);
    if (!$stmt) {
        throw new Exception("Error preparing statement: " . $conn->error);
    }

    $stmt->bind_param("i", $user_id);
    
    if (!$stmt->execute()) {
        throw new Exception("Error executing query: " . $stmt->error);
    }
    
    // Get the result
    $result = $stmt->get_result();
    $pets = array();
    
    // Fetch all pets
    while ($row = $result->fetch_assoc()) {
        // Format the data
        $pet = array(
            'id' => (int)$row['id'],
            'user_id' => (int)$row['user_id'],
            'name' => $row['name'],
            'type' => $row['type'],
            'age' => (int)$row['age'],
            'price' => (float)$row['price'],
            'description' => $row['description'],
            'image_url' => $row['image_url'],
            'created_at' => $row['created_at']
        );
        
        // Add base URL to image if it's not empty and doesn't start with http
        if (!empty($pet['image_url']) && strpos($pet['image_url'], 'http') !== 0) {
            $pet['image_url'] = 'http://10.0.2.2/petopia/' . $pet['image_url'];
        }
        
        $pets[] = $pet;
    }
    
    // Set success response
    $response['success'] = true;
    $response['message'] = 'Pets retrieved successfully';
    $response['pets'] = $pets;
    
    $stmt->close();
    
} catch (Exception $e) {
    // Log error
    error_log("Error in pets.php: " . $e->getMessage());
    
    // Set error response
    $response['success'] = false;
    $response['message'] = 'Error: ' . $e->getMessage();
}

// Send response
echo json_encode($response);

// Close connection
$conn->close();
?>
