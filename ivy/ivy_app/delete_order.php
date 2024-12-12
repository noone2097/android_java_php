<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

require_once 'db_connection.php';

// Get JSON input
$json = file_get_contents('php://input');
$data = json_decode($json, true);

if (!isset($data['order_id'])) {
    echo json_encode(['success' => false, 'message' => 'Order ID is required']);
    exit;
}

try {
    // Start transaction
    $conn->begin_transaction();

    // Delete order items first (due to foreign key constraint)
    $stmt = $conn->prepare("DELETE FROM order_items WHERE order_id = ?");
    if (!$stmt) {
        throw new Exception("Prepare items delete failed: " . $conn->error);
    }
    
    $stmt->bind_param("i", $data['order_id']);
    
    if (!$stmt->execute()) {
        throw new Exception("Execute items delete failed: " . $stmt->error);
    }

    // Then delete the order
    $stmt = $conn->prepare("DELETE FROM orders WHERE order_id = ? AND status = 'paid'");
    if (!$stmt) {
        throw new Exception("Prepare order delete failed: " . $conn->error);
    }
    
    $stmt->bind_param("i", $data['order_id']);
    
    if (!$stmt->execute()) {
        throw new Exception("Execute order delete failed: " . $stmt->error);
    }

    if ($stmt->affected_rows === 0) {
        throw new Exception("Order not found or not paid");
    }

    // Commit transaction
    $conn->commit();
    
    echo json_encode([
        'success' => true,
        'message' => 'Order deleted successfully'
    ]);

} catch (Exception $e) {
    // Rollback on error
    $conn->rollback();
    echo json_encode([
        'success' => false,
        'message' => 'Error deleting order: ' . $e->getMessage()
    ]);
}

$conn->close();
?>
