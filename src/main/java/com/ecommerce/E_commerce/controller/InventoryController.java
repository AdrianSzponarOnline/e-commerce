package com.ecommerce.E_commerce.controller;

import com.ecommerce.E_commerce.dto.inventory.InventoryCreateDTO;
import com.ecommerce.E_commerce.dto.inventory.InventoryDTO;
import com.ecommerce.E_commerce.dto.inventory.InventorySummaryDTO;
import com.ecommerce.E_commerce.dto.inventory.InventoryUpdateDTO;
import com.ecommerce.E_commerce.service.InventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<InventoryDTO> createInventory(@Valid @RequestBody InventoryCreateDTO dto) {
        InventoryDTO inventory = inventoryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(inventory);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<InventoryDTO> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody InventoryUpdateDTO dto) {
        InventoryDTO inventory = inventoryService.update(id, dto);
        return ResponseEntity.ok(inventory);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        inventoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<InventoryDTO> getInventoryById(@PathVariable Long id) {
        InventoryDTO inventory = inventoryService.getById(id);
        return ResponseEntity.ok(inventory);
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<InventoryDTO> getInventoryByProductId(@PathVariable Long productId) {
        InventoryDTO inventory = inventoryService.getByProductId(productId);
        return ResponseEntity.ok(inventory);
    }
    
    @GetMapping
    public ResponseEntity<Page<InventoryDTO>> getAllInventory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<InventoryDTO> inventories = inventoryService.findAll(pageable);
        return ResponseEntity.ok(inventories);
    }
    
    @GetMapping("/summary")
    public ResponseEntity<Page<InventorySummaryDTO>> getAllInventorySummary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<InventorySummaryDTO> inventories = inventoryService.findAllSummary(pageable);
        return ResponseEntity.ok(inventories);
    }
    
    @GetMapping("/product/{productId}/available")
    public ResponseEntity<Boolean> checkStockAvailability(
            @PathVariable Long productId,
            @RequestParam @Min(value = 1, message = "Quantity must be at least 1") Integer quantity) {
        boolean available = inventoryService.isStockAvailable(productId, quantity);
        return ResponseEntity.ok(available);
    }
}

