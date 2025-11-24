package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.inventory.InventoryCreateDTO;
import com.ecommerce.E_commerce.dto.inventory.InventoryDTO;
import com.ecommerce.E_commerce.dto.inventory.InventorySummaryDTO;
import com.ecommerce.E_commerce.dto.inventory.InventoryUpdateDTO;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryService {
    
    // CRUD Operations
    InventoryDTO create(InventoryCreateDTO dto);
    
    InventoryDTO update(Long id, InventoryUpdateDTO dto);
    
    void delete(Long id);
    
    InventoryDTO getById(Long id);
    
    InventoryDTO getByProductId(Long productId);
    
    Page<InventoryDTO> findAll(Pageable pageable);
    
    Page<InventorySummaryDTO> findAllSummary(Pageable pageable);
    
    // Stock Management Operations
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

