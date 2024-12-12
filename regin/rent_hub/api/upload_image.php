<?php
function saveImage($base64Image) {
    // Create uploads directory if it doesn't exist
    $uploadDir = __DIR__ . '/uploads/';
    if (!file_exists($uploadDir)) {
        mkdir($uploadDir, 0777, true);
    }

    // Extract image data from base64 string
    $base64Image = str_replace('data:image/jpeg;base64,', '', $base64Image);
    $base64Image = str_replace(' ', '+', $base64Image);
    $imageData = base64_decode($base64Image);

    // Generate unique filename
    $filename = uniqid() . '.jpg';
    $filePath = $uploadDir . $filename;

    // Save the image
    if (file_put_contents($filePath, $imageData)) {
        // Return the relative URL
        return 'uploads/' . $filename;
    }

    return null;
}
?>
