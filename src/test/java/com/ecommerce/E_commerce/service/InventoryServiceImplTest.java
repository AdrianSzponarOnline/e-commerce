package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.exception.InsufficientStockException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.model.Inventory;
import com.ecommerce.E_commerce.model.Product;
import com.ecommerce.E_commerce.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    private InventoryServiceImpl inventoryService;

    private Inventory testInventory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryServiceImpl(inventoryRepository);

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");

        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setProduct(testProduct);
        testInventory.setAvailableQuantity(100);
        testInventory.setReservedQuantity(0);
        testInventory.setIsActive(true);
    }

    @Test
    void reserveStock_ShouldReserveStockSuccessfully() {
        // Given
        when(inventoryRepository.findByProductIdWithLock(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        inventoryService.reserveStock(1L, 10);

        // Then
        assertEquals(90, testInventory.getAvailableQuantity());
        assertEquals(10, testInventory.getReservedQuantity());
        verify(inventoryRepository).findByProductIdWithLock(1L);
        verify(inventoryRepository).save(testInventory);
    }

    @Test
    void reserveStock_ShouldThrowException_WhenInsufficientStock() {
        // Given
        testInventory.setAvailableQuantity(5);
        when(inventoryRepository.findByProductIdWithLock(1L)).thenReturn(Optional.of(testInventory));

        // When & Then
        InsufficientStockException exception = assertThrows(InsufficientStockException.class,
                () -> inventoryService.reserveStock(1L, 10));
        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void reserveStock_ShouldThrowException_WhenInventoryNotFound() {
        // Given
        when(inventoryRepository.findByProductIdWithLock(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> inventoryService.reserveStock(1L, 10));
        assertTrue(exception.getMessage().contains("Inventory not found"));
    }

    @Test
    void reserveStock_ShouldThrowException_WhenInventoryNotActive() {
        // Given
        testInventory.setIsActive(false);
        when(inventoryRepository.findByProductIdWithLock(1L)).thenReturn(Optional.of(testInventory));

        // When & Then
        InsufficientStockException exception = assertThrows(InsufficientStockException.class,
                () -> inventoryService.reserveStock(1L, 10));
        assertTrue(exception.getMessage().contains("not active"));
    }

    @Test
    void releaseStock_ShouldReleaseStockSuccessfully() {
        // Given
        testInventory.setReservedQuantity(10);
        testInventory.setAvailableQuantity(90);
        when(inventoryRepository.findByProductIdWithLock(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        inventoryService.releaseStock(1L, 5);

        // Then
        assertEquals(95, testInventory.getAvailableQuantity());
        assertEquals(5, testInventory.getReservedQuantity());
        verify(inventoryRepository).save(testInventory);
    }

    @Test
    void releaseStock_ShouldThrowException_WhenInsufficientReservedStock() {
        // Given
        testInventory.setReservedQuantity(5);
        when(inventoryRepository.findByProductIdWithLock(1L)).thenReturn(Optional.of(testInventory));

        // When & Then
        InsufficientStockException exception = assertThrows(InsufficientStockException.class,
                () -> inventoryService.releaseStock(1L, 10));
        assertTrue(exception.getMessage().contains("Insufficient reserved stock"));
    }

    @Test
    void finalizeReservation_ShouldFinalizeReservationSuccessfully() {
        // Given
        testInventory.setReservedQuantity(10);
        when(inventoryRepository.findByProductIdWithLock(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        inventoryService.finalizeReservation(1L, 5);

        // Then
        assertEquals(5, testInventory.getReservedQuantity());
        verify(inventoryRepository).save(testInventory);
    }

    @Test
    void finalizeReservation_ShouldThrowException_WhenInsufficientReservedStock() {
        // Given
        testInventory.setReservedQuantity(5);
        when(inventoryRepository.findByProductIdWithLock(1L)).thenReturn(Optional.of(testInventory));

        // When & Then
        InsufficientStockException exception = assertThrows(InsufficientStockException.class,
                () -> inventoryService.finalizeReservation(1L, 10));
        assertTrue(exception.getMessage().contains("Insufficient reserved stock"));
    }

    @Test
    void isStockAvailable_ShouldReturnTrue_WhenStockIsAvailable() {
        // Given
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.isStockAvailable(1L, 50);

        // Then
        assertTrue(result);
        verify(inventoryRepository).findByProductId(1L);
    }

    @Test
    void isStockAvailable_ShouldReturnFalse_WhenStockIsNotAvailable() {
        // Given
        testInventory.setAvailableQuantity(10);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.isStockAvailable(1L, 50);

        // Then
        assertFalse(result);
    }

    @Test
    void isStockAvailable_ShouldReturnFalse_WhenInventoryNotFound() {
        // Given
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.empty());

        // When
        boolean result = inventoryService.isStockAvailable(1L, 10);

        // Then
        assertFalse(result);
    }

    @Test
    void isStockAvailable_ShouldReturnFalse_WhenInventoryNotActive() {
        // Given
        testInventory.setIsActive(false);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(testInventory));

        // When
        boolean result = inventoryService.isStockAvailable(1L, 10);

        // Then
        assertFalse(result);
    }
}

