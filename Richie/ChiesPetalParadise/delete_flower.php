<?php
include_once 'config.php';

try {
    // Get flower ID from request
    $json = file_get_contents('php://input');
    $data = json_decode($json, true);
    
    if (!isset($data['id'])) {
        throw new Exception("Flower ID is required");
    }
    
    $id = $data['id'];
    
    // Get image path before deleting the record
    $stmt = $conn->prepare("SELECT image_url FROM flowers WHERE id = ?");
    $stmt->execute([$id]);
    $imagePath = $stmt->fetchColumn();
    
    // Delete the record
    $stmt = $conn->prepare("DELETE FROM flowers WHERE id = ?");
    $stmt->execute([$id]);
    
    // Delete the image file if it exists
    if ($imagePath && file_exists($imagePath)) {
        unlink($imagePath);
    }
    
    http_response_code(200);
    echo json_encode(array("message" => "Flower deleted successfully"));
    
} catch(Exception $e) {
    error_log("Error: " . $e->getMessage());
    http_response_code(503);
    echo json_encode(array("message" => "Unable to delete flower: " . $e->getMessage()));
}
?>
