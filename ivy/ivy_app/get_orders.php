<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: GET");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

require_once 'db_connection.php';

// Get username from query parameter
$username = isset($_GET['username']) ? $_GET['username'] : '';

try {
    // Get orders
    $query = "SELECT * FROM orders WHERE username = ? ORDER BY order_date DESC";
    $stmt = $conn->prepare($query);
    
    if (!$stmt) {
        throw new Exception("Prepare failed: " . $conn->error);
    }
    
    $stmt->bind_param("s", $username);
    
    if (!$stmt->execute()) {
        throw new Exception("Execute failed: " . $stmt->error);
    }
    
    $result = $stmt->get_result();
    $orders = array();
    
    while ($row = $result->fetch_assoc()) {
        // Get items for this order
        $itemsQuery = "SELECT * FROM order_items WHERE order_id = ?";
        $itemsStmt = $conn->prepare($itemsQuery);
        $itemsStmt->bind_param("i", $row['order_id']);
        $itemsStmt->execute();
        $itemsResult = $itemsStmt->get_result();
        
        $items = array();
        while ($itemRow = $itemsResult->fetch_assoc()) {
            $items[] = $itemRow;
        }
        
        // Format the date
        $row['order_date'] = date('M d, Y h:i A', strtotime($row['order_date']));
        // Format the currency values
        $row['subtotal'] = number_format($row['subtotal'], 2);
        $row['discount'] = number_format($row['discount'], 2);
        $row['total'] = number_format($row['total'], 2);
        
        // Add items to order
        $row['items'] = $items;
        $orders[] = $row;
    }
    
    echo json_encode([
        'success' => true,
        'orders' => $orders
    ]);

} catch (Exception $e) {
    $error_msg = "Error retrieving orders: " . $e->getMessage();
    error_log($error_msg);
    echo json_encode([
        'success' => false,
        'message' => $error_msg
    ]);
}

$conn->close();
?>
