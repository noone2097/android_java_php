<?php
include_once 'config.php';

try {
    // Add tax column to flowers table
    $sql = "ALTER TABLE flowers 
            ADD COLUMN tax_rate DECIMAL(4,2) DEFAULT 12.00 
            AFTER price";
    
    $conn->exec($sql);
    echo "Added tax_rate column to flowers table successfully\n";
    
    // Update existing records to have 12% tax
    $sql = "UPDATE flowers SET tax_rate = 12.00 WHERE tax_rate IS NULL";
    $conn->exec($sql);
    echo "Updated existing records with default tax rate\n";
    
} catch(PDOException $e) {
    echo "Error: " . $e->getMessage() . "\n";
}
?>
