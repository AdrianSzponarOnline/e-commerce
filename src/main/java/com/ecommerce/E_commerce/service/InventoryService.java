package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.exception.ResourceNotFoundException;

public interface InventoryService {
    
    /**
     * Reserves stock for a product (decreases available quantity, increases reserved quantity).
     * 
     * @param productId The product ID
     * @param quantity The quantity to reserve
     * @throws ResourceNotFoundException if inventory not found for product
     * @throws IllegalArgumentException if inventory is not active or insufficient stock available
     */
    void reserveStock(Long productId, Integer quantity);
    
    /**
     * Releases reserved stock back to available (increases available quantity, decreases reserved quantity).
     * 
     * @param productId The product ID
     * @param quantity The quantity to release
     * @throws ResourceNotFoundException if inventory not found for product
     * @throws IllegalArgumentException if inventory is not active or insufficient reserved quantity
     */
    void releaseStock(Long productId, Integer quantity);
    
    /**
     * Finalizes reserved stock (decreases reserved quantity only, stock is sold).
     * Used when order is confirmed/shipped/delivered.
     * 
     * @param productId The product ID
     * @param quantity The quantity to finalize
     * @throws ResourceNotFoundException if inventory not found for product
     * @throws IllegalArgumentException if inventory is not active or insufficient reserved quantity
     */
    void finalizeReservation(Long productId, Integer quantity);
    
    /**
     * Checks if enough stock is available for a product.
     * 
     * @param productId The product ID
     * @param quantity The required quantity
     * @return true if enough stock is available, false otherwise
     * @throws ResourceNotFoundException if inventory not found for product
     */
    boolean isStockAvailable(Long productId, Integer quantity);
}

