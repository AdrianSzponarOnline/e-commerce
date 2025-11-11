package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.model.Inventory;
import com.ecommerce.E_commerce.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {
    
    private final InventoryRepository inventoryRepository;
    
    @Autowired
    public InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }
    
    @Override
    public void reserveStock(Long productId, Integer quantity) {
        Inventory inventory = getActiveInventory(productId);
        
        if (inventory.getAvailableQuantity() < quantity) {
            throw new IllegalArgumentException(
                String.format("Insufficient stock for product id %d. Available: %d, Requested: %d",
                    productId, inventory.getAvailableQuantity(), quantity));
        }
        
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventoryRepository.save(inventory);
    }
    
    @Override
    public void releaseStock(Long productId, Integer quantity) {
        Inventory inventory = getActiveInventory(productId);
        
        if (inventory.getReservedQuantity() < quantity) {
            throw new IllegalArgumentException(
                String.format("Insufficient reserved stock for product id %d. Reserved: %d, Requested: %d",
                    productId, inventory.getReservedQuantity(), quantity));
        }
        
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        inventoryRepository.save(inventory);
    }
    
    @Override
    public void finalizeReservation(Long productId, Integer quantity) {
        Inventory inventory = getActiveInventory(productId);
        
        if (inventory.getReservedQuantity() < quantity) {
            throw new IllegalArgumentException(
                String.format("Insufficient reserved stock for product id %d. Reserved: %d, Requested: %d",
                    productId, inventory.getReservedQuantity(), quantity));
        }
        
        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        inventoryRepository.save(inventory);
    }
    
    @Override
    public boolean isStockAvailable(Long productId, Integer quantity) {
        try {
            // Use regular find (no lock needed for read-only check)
            Inventory inventory = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + productId));
            
            if (!inventory.getIsActive()) {
                return false;
            }
            
            return inventory.getAvailableQuantity() >= quantity;
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            return false;
        }
    }
    
    private Inventory getActiveInventory(Long productId) {
        Inventory inventory = inventoryRepository.findByProductIdWithLock(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + productId));
        
        if (!inventory.getIsActive()) {
            throw new IllegalArgumentException("Inventory is not active for product id: " + productId);
        }
        return inventory;
    }
}

