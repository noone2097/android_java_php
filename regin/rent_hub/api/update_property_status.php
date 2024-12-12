<?php
require_once 'config.php';
require_once 'db_connect.php';

header('Content-Type: application/json');

// Check if it's a POST request
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
    exit;
}

// Get POST data
$data = json_decode(file_get_contents('php://input'), true);

if (!isset($data['property_id']) || !isset($data['status'])) {
    http_response_code(400);
    echo json_encode(['error' => 'Missing required fields']);
    exit;
}

$propertyId = $data['property_id'];
$status = $data['status'];

// Validate status
if (!in_array($status, ['available', 'rented'])) {
    http_response_code(400);
    echo json_encode(['error' => 'Invalid status value']);
    exit;
}

try {
    $stmt = $conn->prepare("UPDATE properties SET status = ? WHERE id = ?");
    $stmt->bind_param("si", $status, $propertyId);
    
    if ($stmt->execute()) {
        echo json_encode(['success' => true, 'message' => 'Property status updated successfully']);
    } else {
        throw new Exception("Error updating property status");
    }
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['error' => $e->getMessage()]);
}

$stmt->close();
$conn->close();
?>
