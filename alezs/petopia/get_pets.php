<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include_once 'config.php';

// Get JSON input
$data = json_decode(file_get_contents("php://input"), true);
$user_id = isset($data['user_id']) ? $data['user_id'] : null;

if (!$user_id) {
    echo json_encode([
        'success' => false,
        'message' => 'User ID is required'
    ]);
    exit();
}

try {
    $query = "SELECT * FROM pets WHERE user_id = ?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param("i", $user_id);
    
    if ($stmt->execute()) {
        $result = $stmt->get_result();
        $pets = [];
        
        while ($row = $result->fetch_assoc()) {
            // Format the created_at date
            $created_at = date('Y-m-d H:i:s', strtotime($row['created_at']));
            
            // Make sure description is not null
            $description = $row['description'] ?? '';
            
            $pets[] = [
                'id' => (int)$row['id'],
                'user_id' => (int)$row['user_id'],
                'name' => $row['name'],
                'type' => $row['type'],
                'age' => (int)$row['age'],
                'price' => (float)$row['price'],
                'description' => $description,
                'image_url' => $row['image_url'],
                'created_at' => $created_at
            ];
        }
        
        echo json_encode([
            'success' => true,
            'pets' => $pets
        ]);
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'Error fetching pets: ' . $stmt->error
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
