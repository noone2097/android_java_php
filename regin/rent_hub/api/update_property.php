<?php
require_once 'config.php';
require_once 'db_connect.php';

header('Content-Type: application/json');

// Check if it's a PUT request
if ($_SERVER['REQUEST_METHOD'] !== 'PUT') {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
    exit;
}

// Get PUT data
$data = json_decode(file_get_contents('php://input'), true);
$id = isset($_GET['id']) ? $_GET['id'] : null;

if (!$id || !isset($data['title']) || !isset($data['location']) || !isset($data['price'])) {
    http_response_code(400);
    echo json_encode(['error' => 'Missing required fields']);
    exit;
}

try {
    $stmt = $conn->prepare("UPDATE properties SET title = ?, location = ?, price = ?, description = ? WHERE id = ?");
    $stmt->bind_param("ssdsi", 
        $data['title'],
        $data['location'],
        $data['price'],
        $data['description'],
        $id
    );
    
    if ($stmt->execute()) {
        echo json_encode([
            'success' => true,
            'message' => 'Property updated successfully'
        ]);
    } else {
        throw new Exception("Error updating property");
    }
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['error' => $e->getMessage()]);
}

$stmt->close();
$conn->close();
?>
