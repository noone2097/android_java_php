<?php
header('Content-Type: application/json');

// Enable error reporting for debugging
ini_set('display_errors', 1);
error_reporting(E_ALL);

// Get JSON input
$json = file_get_contents('php://input');
$data = json_decode($json);

// Log received data
error_log("Received data: " . print_r($data, true));

// Check if required data is present
if (!isset($data->user_id) || !isset($data->name) || !isset($data->type) || 
    !isset($data->age) || !isset($data->price) || !isset($data->description) || 
    !isset($data->image)) {
    echo json_encode([
        'success' => false,
        'message' => 'Missing required fields'
    ]);
    exit;
}

// Database connection
require_once 'config.php';

try {
    // Create images directory if it doesn't exist
    $upload_dir = __DIR__ . '/images/';
    if (!file_exists($upload_dir)) {
        mkdir($upload_dir, 0777, true);
    }

    // Handle image data
    $image_data = $data->image;
    
    // Check if it's a content URI
    if (strpos($image_data, 'content://') === 0) {
        echo json_encode([
            'success' => false,
            'message' => 'Invalid image format. Please send base64 encoded image data.'
        ]);
        exit;
    }

    // Remove data URI prefix if present
    $image_data = str_replace('data:image/jpeg;base64,', '', $image_data);
    $image_data = str_replace('data:image/png;base64,', '', $image_data);
    
    // Decode base64 image
    $image_binary = base64_decode($image_data);
    if ($image_binary === false) {
        throw new Exception("Invalid base64 image data");
    }

    // Generate unique filename
    $image_filename = uniqid('pet_') . '.jpg';
    $image_path = $upload_dir . $image_filename;
    
    // Save image file
    if (file_put_contents($image_path, $image_binary) === false) {
        throw new Exception("Failed to save image file");
    }

    // Image URL that will be stored in database
    $image_url = 'images/' . $image_filename;

    // Begin transaction
    $conn->begin_transaction();

    try {
        // Prepare SQL statement
        $stmt = $conn->prepare("INSERT INTO pets (user_id, name, type, age, price, description, image_url) 
                               VALUES (?, ?, ?, ?, ?, ?, ?)");
        
        // Bind parameters
        $stmt->bind_param("issiiss", 
            $data->user_id,
            $data->name,
            $data->type,
            $data->age,
            $data->price,
            $data->description,
            $image_url
        );
        
        // Execute the statement
        if (!$stmt->execute()) {
            throw new Exception("Error executing statement: " . $stmt->error);
        }

        $pet_id = $stmt->insert_id;
        
        // Commit transaction
        $conn->commit();
        
        echo json_encode([
            'success' => true,
            'message' => 'Pet added successfully',
            'pet_id' => $pet_id,
            'image_url' => $image_url
        ]);
        
        $stmt->close();
        
    } catch (Exception $e) {
        // Rollback transaction on error
        $conn->rollback();
        // Delete uploaded image if database insert failed
        if (file_exists($image_path)) {
            unlink($image_path);
        }
        throw $e;
    }
    
} catch (Exception $e) {
    error_log("Error in add_pet.php: " . $e->getMessage());
    echo json_encode([
        'success' => false,
        'message' => 'Error adding pet: ' . $e->getMessage()
    ]);
}

$conn->close();
?>
