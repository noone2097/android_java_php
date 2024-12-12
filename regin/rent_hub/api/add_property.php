<?php
// Start output buffering to prevent any unwanted output
ob_start();

// Set headers first
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Access-Control-Allow-Headers, Content-Type, Access-Control-Allow-Methods, Authorization, X-Requested-With');

// Include database connection and image upload function
require_once 'db_connect.php';
require_once 'upload_image.php';

// Log file setup
$logFile = __DIR__ . '/debug.log';
function writeLog($message) {
    global $logFile;
    $timestamp = date('Y-m-d H:i:s');
    file_put_contents($logFile, "[$timestamp] $message\n", FILE_APPEND);
}

try {
    writeLog("Request received");

    // Get posted data
    $raw_input = file_get_contents("php://input");
    writeLog("Raw input received, length: " . strlen($raw_input));

    $data = json_decode($raw_input, true);
    if (json_last_error() !== JSON_ERROR_NONE) {
        throw new Exception("JSON decode error: " . json_last_error_msg());
    }

    // Validate required fields
    $required_fields = ['title', 'description', 'price', 'location', 'owner_id'];
    $missing_fields = [];
    foreach ($required_fields as $field) {
        if (!isset($data[$field])) {
            $missing_fields[] = $field;
        }
    }

    if (!empty($missing_fields)) {
        throw new Exception("Missing required fields: " . implode(', ', $missing_fields));
    }

    // Handle image upload if present
    $image_url = '';
    if (isset($data['image_url']) && !empty($data['image_url'])) {
        writeLog("Processing image upload");
        $image_url = saveImage($data['image_url']);
        if (!$image_url) {
            throw new Exception("Failed to save image");
        }
        writeLog("Image saved successfully: " . $image_url);
    }

    // Prepare the SQL statement
    $sql = "INSERT INTO properties (title, description, price, location, image_url, owner_id) 
            VALUES (?, ?, ?, ?, ?, ?)";
    
    writeLog("SQL Query: " . $sql);
    
    $stmt = $conn->prepare($sql);
    if (!$stmt) {
        throw new Exception("Prepare failed: " . $conn->error);
    }
    
    $stmt->bind_param("ssdssi", 
        $data['title'],
        $data['description'],
        $data['price'],
        $data['location'],
        $image_url,
        $data['owner_id']
    );

    writeLog("Attempting to execute query with values: " . 
             "title=" . $data['title'] . ", " .
             "description=" . $data['description'] . ", " .
             "price=" . $data['price'] . ", " .
             "location=" . $data['location'] . ", " .
             "image_url=" . $image_url . ", " .
             "owner_id=" . $data['owner_id']);

    if (!$stmt->execute()) {
        throw new Exception("Execute failed: " . $stmt->error);
    }

    $response = [
        'success' => true,
        'message' => 'Property added successfully',
        'property_id' => $conn->insert_id,
        'image_url' => $image_url
    ];
    
    writeLog("Query executed successfully. Property ID: " . $conn->insert_id);
    
    // Clean any output buffers before sending response
    ob_end_clean();
    echo json_encode($response);

} catch (Exception $e) {
    writeLog("Error: " . $e->getMessage());
    
    // Clean any output buffers before sending response
    ob_end_clean();
    echo json_encode([
        'success' => false,
        'message' => $e->getMessage()
    ]);
}

$stmt->close();
$conn->close();
writeLog("Connection closed");
?>
