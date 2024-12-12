<?php
require_once 'config.php';
require_once 'db_connect.php';

header('Content-Type: application/json');

$userId = isset($_GET['user_id']) ? $_GET['user_id'] : null;

if (!$userId) {
    http_response_code(400);
    echo json_encode(['error' => 'User ID is required']);
    exit;
}

try {
    // Get statistics with conditional sum based on status
    $stmt = $conn->prepare("
        SELECT 
            COUNT(*) as total_properties,
            SUM(CASE WHEN status = 'Available' THEN price ELSE 0 END) as total_revenue,
            AVG(CASE WHEN status = 'Available' THEN price ELSE NULL END) as average_price,
            SUM(CASE WHEN status = 'Rented' THEN 1 ELSE 0 END) as rented_count
        FROM properties 
        WHERE owner_id = ?
    ");
    
    $stmt->bind_param("i", $userId);
    $stmt->execute();
    $result = $stmt->get_result();
    $stats = $result->fetch_assoc();

    // Handle null values and format response
    $response = [
        'total_properties' => (int)$stats['total_properties'],
        'total_revenue' => $stats['total_revenue'] ? (float)$stats['total_revenue'] : 0,
        'average_price' => $stats['average_price'] ? (float)$stats['average_price'] : 0,
        'rented_count' => (int)$stats['rented_count']
    ];

    echo json_encode($response);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['error' => $e->getMessage()]);
}

$stmt->close();
$conn->close();
?>
