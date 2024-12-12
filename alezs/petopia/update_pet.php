<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include_once 'config.php';

// Get JSON input
$data = json_decode(file_get_contents("php://input"), true);

// Extract data
$id = isset($data['id']) ? $data['id'] : '';
$name = isset($data['name']) ? $data['name'] : '';
$type = isset($data['type']) ? $data['type'] : '';
$age = isset($data['age']) ? $data['age'] : '';
$price = isset($data['price']) ? $data['price'] : '';
$description = isset($data['description']) ? $data['description'] : '';

// Validate input
if (empty($id) || empty($name) || empty($type) || empty($age) || empty($price)) {
    echo json_encode([
        'success' => false,
        'message' => 'Please provide all required fields'
    ]);
    exit();
}

try {
    // Prepare the SQL query
    $query = "UPDATE pets SET 
              name = ?, 
              type = ?, 
              age = ?, 
              price = ?, 
              description = ?
              WHERE id = ?";
    
    $stmt = $conn->prepare($query);
    $stmt->bind_param("ssidsi", $name, $type, $age, $price, $description, $id);
    
    // Execute the query
    if ($stmt->execute()) {
        if ($stmt->affected_rows > 0) {
            echo json_encode([
                'success' => true,
                'message' => 'Pet updated successfully'
            ]);
        } else {
            echo json_encode([
                'success' => false,
                'message' => 'No changes made to the pet'
            ]);
        }
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'Error updating pet: ' . $stmt->error
        ]);
    }
    
    $stmt->close();
} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage()
    ]);
}

$conn->close();
?>
