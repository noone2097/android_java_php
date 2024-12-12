<?php
include_once 'config.php';

// Get the raw POST data
$json = file_get_contents('php://input');
error_log("Raw input: " . $json);

// Decode the JSON data
$data = json_decode($json, true);

if ($data === null) {
    error_log("JSON decode error: " . json_last_encode_error_msg());
    http_response_code(400);
    echo json_encode(array("message" => "Invalid JSON data"));
    exit();
}

try {
    $id = $data['id'];
    $name = $data['name'];
    $description = $data['description'];
    $price = $data['price'];
    $tax_rate = isset($data['tax_rate']) ? $data['tax_rate'] : 12.00;
    $stock = $data['stock'];
    
    // Check if new image is provided
    if (isset($data['imageUrl']) && !empty($data['imageUrl'])) {
        // Handle new image upload
        $imageData = $data['imageUrl'];
        $decodedImage = base64_decode(preg_replace('#^data:image/\w+;base64,#i', '', $imageData));
        
        // Get old image path to delete later
        $stmt = $conn->prepare("SELECT image_url FROM flowers WHERE id = ?");
        $stmt->execute([$id]);
        $oldImage = $stmt->fetchColumn();
        
        // Create new image
        $uploadDir = "images/";
        $filename = uniqid() . '.jpg';
        $filepath = $uploadDir . $filename;
        
        if (file_put_contents($filepath, $decodedImage) === false) {
            throw new Exception("Failed to save new image");
        }
        
        // Delete old image if it exists
        if ($oldImage && file_exists($oldImage)) {
            unlink($oldImage);
        }
        
        // Update with new image
        $sql = "UPDATE flowers SET name = ?, description = ?, price = ?, tax_rate = ?, stock = ?, image_url = ? WHERE id = ?";
        $stmt = $conn->prepare($sql);
        $stmt->execute([$name, $description, $price, $tax_rate, $stock, $filepath, $id]);
    } else {
        // Update without changing image
        $sql = "UPDATE flowers SET name = ?, description = ?, price = ?, tax_rate = ?, stock = ? WHERE id = ?";
        $stmt = $conn->prepare($sql);
        $stmt->execute([$name, $description, $price, $tax_rate, $stock, $id]);
    }
    
    http_response_code(200);
    echo json_encode(array("message" => "Flower updated successfully"));
    
} catch(Exception $e) {
    error_log("Error: " . $e->getMessage());
    http_response_code(503);
    echo json_encode(array("message" => "Unable to update flower: " . $e->getMessage()));
}
?>
