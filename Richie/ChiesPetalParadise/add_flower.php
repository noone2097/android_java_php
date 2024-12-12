<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include_once 'config.php';

// Enable error reporting
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Log the raw input
$raw_input = file_get_contents("php://input");
error_log("Raw input: " . $raw_input);

$data = json_decode($raw_input, true);
error_log("Decoded data: " . print_r($data, true));

if (!empty($data['name']) && !empty($data['price']) && !empty($data['stock']) && !empty($data['image_data']) && !empty($data['tax_rate'])) {
    try {
        // Create images directory if it doesn't exist
        $upload_dir = 'images/';
        if (!file_exists($upload_dir)) {
            if (!mkdir($upload_dir, 0777, true)) {
                throw new Exception("Failed to create images directory");
            }
        }

        // Log directory creation status
        error_log("Upload directory status: " . (file_exists($upload_dir) ? "exists" : "does not exist"));
        error_log("Upload directory permissions: " . substr(sprintf('%o', fileperms($upload_dir)), -4));

        // Decode and save the image
        $image_data = base64_decode($data['image_data']);
        if ($image_data === false) {
            throw new Exception("Failed to decode base64 image data");
        }

        $image_name = uniqid() . '.jpg';
        $image_path = $upload_dir . $image_name;
        
        // Try to save the image
        if (file_put_contents($image_path, $image_data) === false) {
            throw new Exception("Failed to save image file");
        }

        error_log("Image saved successfully to: " . $image_path);

        // Get the relative path to the image
        $image_url = $image_path;
        error_log("Generated image URL: " . $image_url);

        $query = "INSERT INTO flowers (name, description, price, tax_rate, image_url, stock) VALUES (?, ?, ?, ?, ?, ?)";
        $stmt = $conn->prepare($query);
        
        $stmt->execute([
            $data['name'],
            $data['description'] ?? '',
            $data['price'],
            $data['tax_rate'],
            $image_url,
            $data['stock']
        ]);
        
        http_response_code(201);
        echo json_encode(array(
            "status" => true,
            "message" => "Flower added successfully",
            "id" => $conn->lastInsertId(),
            "image_url" => $image_url
        ));
        
    } catch(Exception $e) {
        error_log("Error in add_flower.php: " . $e->getMessage());
        http_response_code(500);
        echo json_encode(array(
            "status" => false,
            "message" => "Error: " . $e->getMessage()
        ));
    }
} else {
    $missing = array();
    if (empty($data['name'])) $missing[] = 'name';
    if (empty($data['price'])) $missing[] = 'price';
    if (empty($data['stock'])) $missing[] = 'stock';
    if (empty($data['image_data'])) $missing[] = 'image_data';
    if (empty($data['tax_rate'])) $missing[] = 'tax_rate';
    
    error_log("Missing required fields: " . implode(', ', $missing));
    
    http_response_code(400);
    echo json_encode(array(
        "status" => false,
        "message" => "Unable to add flower. The following fields are required: " . implode(', ', $missing)
    ));
}
?>
