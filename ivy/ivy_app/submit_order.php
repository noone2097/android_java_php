<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

require_once 'db_connection.php';

// Get JSON input and log it
$json = file_get_contents('php://input');
error_log("Received JSON: " . $json);

$data = json_decode($json, true);
error_log("Decoded data: " . print_r($data, true));

// Validate input
if (!isset($data['username']) || !isset($data['items']) || empty($data['items'])) {
    $error_msg = "Invalid input data: username or items missing";
    error_log($error_msg);
    echo json_encode(['success' => false, 'message' => $error_msg]);
    exit;
}

try {
    // Start transaction
    $conn->begin_transaction();

    // Log order data
    error_log("Inserting order for username: " . $data['username']);
    error_log("Subtotal: " . $data['subtotal']);
    error_log("Discount: " . $data['discount']);
    error_log("Total: " . $data['total']);

    // Insert order
    $stmt = $conn->prepare("INSERT INTO orders (username, subtotal, discount, total) VALUES (?, ?, ?, ?)");
    if (!$stmt) {
        throw new Exception("Prepare failed: " . $conn->error);
    }
    
    $stmt->bind_param("sddd", 
        $data['username'],
        $data['subtotal'],
        $data['discount'],
        $data['total']
    );
    
    if (!$stmt->execute()) {
        throw new Exception("Execute failed: " . $stmt->error);
    }
    
    $orderId = $conn->insert_id;
    error_log("Order inserted with ID: " . $orderId);

    // Insert order items
    $stmt = $conn->prepare("INSERT INTO order_items (order_id, item_name, quantity, price, subtotal) VALUES (?, ?, ?, ?, ?)");
    if (!$stmt) {
        throw new Exception("Prepare items failed: " . $conn->error);
    }

    foreach ($data['items'] as $item) {
        error_log("Inserting item: " . print_r($item, true));
        
        $stmt->bind_param("isids",
            $orderId,
            $item['productName'],  // Changed from 'name' to 'productName'
            $item['quantity'],
            $item['price'],
            $item['subtotal']
        );
        
        if (!$stmt->execute()) {
            throw new Exception("Execute items failed: " . $stmt->error);
        }
    }

    // Commit transaction
    $conn->commit();
    
    echo json_encode([
        'success' => true,
        'message' => 'Order submitted successfully',
        'orderId' => $orderId
    ]);

} catch (Exception $e) {
    // Rollback on error
    $conn->rollback();
    $error_msg = "Error submitting order: " . $e->getMessage();
    error_log($error_msg);
    echo json_encode([
        'success' => false,
        'message' => $error_msg
    ]);
}

$conn->close();
?>
