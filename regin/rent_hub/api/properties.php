<?php
header('Content-Type: application/json');

include 'config.php';

// Get request method
$method = $_SERVER['REQUEST_METHOD'];

// Get property ID from URL if provided
$id = isset($_GET['id']) ? $_GET['id'] : null;

switch($method) {
    case 'GET':
        // List all properties or get single property
        if ($id) {
            // Get single property
            $stmt = $conn->prepare("SELECT * FROM properties WHERE id = ?");
            $stmt->bind_param("i", $id);
        } else {
            // Get all properties
            $owner_id = isset($_GET['owner_id']) ? $_GET['owner_id'] : null;
            if ($owner_id) {
                $stmt = $conn->prepare("SELECT * FROM properties WHERE owner_id = ? ORDER BY created_at DESC");
                $stmt->bind_param("i", $owner_id);
            } else {
                $stmt = $conn->prepare("SELECT * FROM properties ORDER BY created_at DESC");
            }
        }
        
        $stmt->execute();
        $result = $stmt->get_result();
        $properties = [];
        
        while($row = $result->fetch_assoc()) {
            $properties[] = $row;
        }
        
        echo json_encode($id ? ($properties[0] ?? null) : $properties);
        break;

    case 'POST':
        // Add new property
        $data = json_decode(file_get_contents('php://input'), true);
        
        // Log the received data for debugging
        error_log("Received data: " . print_r($data, true));
        
        $stmt = $conn->prepare("INSERT INTO properties (title, location, price, description, image, owner_id) VALUES (?, ?, ?, ?, ?, ?)");
        $stmt->bind_param("ssdssi", 
            $data['title'],
            $data['location'],
            $data['price'],
            $data['description'],
            $data['image'],
            $data['owner_id']
        );
        
        if ($stmt->execute()) {
            echo json_encode([
                'success' => true,
                'message' => 'Property added successfully',
                'id' => $conn->insert_id
            ]);
        } else {
            error_log("MySQL Error: " . $stmt->error);
            http_response_code(500);
            echo json_encode([
                'success' => false,
                'message' => 'Error adding property: ' . $stmt->error
            ]);
        }
        break;

    case 'PUT':
        // Update existing property
        if (!$id) {
            http_response_code(400);
            echo json_encode([
                'success' => false,
                'message' => 'Property ID is required'
            ]);
            break;
        }

        $data = json_decode(file_get_contents('php://input'), true);
        
        $stmt = $conn->prepare("UPDATE properties SET title = ?, location = ?, price = ?, description = ? WHERE id = ?");
        $stmt->bind_param("ssdsi",
            $data['title'],
            $data['location'],
            $data['price'],
            $data['description'],
            $id
        );
        
        if ($stmt->execute()) {
            echo json_encode([
                'success' => true,
                'message' => 'Property updated successfully'
            ]);
        } else {
            http_response_code(500);
            echo json_encode([
                'success' => false,
                'message' => 'Error updating property: ' . $stmt->error
            ]);
        }
        break;

    case 'DELETE':
        // Delete property
        if (!$id) {
            http_response_code(400);
            echo json_encode([
                'success' => false,
                'message' => 'Property ID is required'
            ]);
            break;
        }
        
        $stmt = $conn->prepare("DELETE FROM properties WHERE id = ?");
        $stmt->bind_param("i", $id);
        
        if ($stmt->execute()) {
            echo json_encode([
                'success' => true,
                'message' => 'Property deleted successfully'
            ]);
        } else {
            http_response_code(500);
            echo json_encode([
                'success' => false,
                'message' => 'Error deleting property: ' . $stmt->error
            ]);
        }
        break;
        
    default:
        http_response_code(405);
        echo json_encode([
            'success' => false,
            'message' => 'Method not allowed'
        ]);
        break;
}

$conn->close();
?>
