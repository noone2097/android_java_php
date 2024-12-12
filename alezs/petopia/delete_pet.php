<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

require_once 'config.php';

// Initialize response array
$response = array('success' => false, 'message' => '');

try {
    // Get JSON input
    $json = file_get_contents('php://input');
    $data = json_decode($json);

    // Validate input
    if (!isset($data->pet_id) || !isset($data->user_id)) {
        throw new Exception("Missing required fields");
    }

    // Convert to integers
    $pet_id = (int)$data->pet_id;
    $user_id = (int)$data->user_id;

    // Validate IDs
    if ($pet_id <= 0 || $user_id <= 0) {
        throw new Exception("Invalid ID values");
    }

    // Start transaction
    $conn->begin_transaction();

    // Check if the pet belongs to the user
    $stmt = $conn->prepare("SELECT id FROM pets WHERE id = ? AND user_id = ?");
    $stmt->bind_param("ii", $pet_id, $user_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows === 0) {
        throw new Exception("Pet not found or you don't have permission to delete it");
    }

    // Delete the pet
    $stmt = $conn->prepare("DELETE FROM pets WHERE id = ? AND user_id = ?");
    $stmt->bind_param("ii", $pet_id, $user_id);
    
    if (!$stmt->execute()) {
        throw new Exception("Failed to delete pet: " . $stmt->error);
    }

    // Commit transaction
    $conn->commit();

    // Set success response
    $response['success'] = true;
    $response['message'] = 'Pet deleted successfully';

} catch (Exception $e) {
    // Rollback transaction if active
    if ($conn->inTransaction()) {
        $conn->rollback();
    }

    // Set error response
    $response['success'] = false;
    $response['message'] = $e->getMessage();

    // Log error
    error_log("Error in delete_pet.php: " . $e->getMessage());
}

// Send response
echo json_encode($response);

// Close connection
$conn->close();
?>
