<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: GET");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include_once 'config.php';

try {
    $query = "SELECT * FROM flowers ORDER BY created_at DESC";
    $stmt = $conn->prepare($query);
    $stmt->execute();
    
    $flowers = array();
    
    while($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        $flower = array(
            "id" => $row['id'],
            "name" => $row['name'],
            "description" => $row['description'],
            "price" => floatval($row['price']),
            "image_url" => $row['image_url'],
            "stock" => intval($row['stock']),
            "created_at" => $row['created_at']
        );
        array_push($flowers, $flower);
    }
    
    http_response_code(200);
    echo json_encode(array(
        "status" => true,
        "message" => "Flowers retrieved successfully",
        "flowers" => $flowers
    ));
    
} catch(PDOException $e) {
    http_response_code(500);
    echo json_encode(array(
        "status" => false,
        "message" => "Error: " . $e->getMessage()
    ));
}
?>
