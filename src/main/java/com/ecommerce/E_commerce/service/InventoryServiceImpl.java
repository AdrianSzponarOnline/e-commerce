package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.inventory.InventoryCreateDTO;
import com.ecommerce.E_commerce.dto.inventory.InventoryDTO;
import com.ecommerce.E_commerce.dto.inventory.InventorySummaryDTO;
import com.ecommerce.E_commerce.dto.inventory.InventoryUpdateDTO;
import com.ecommerce.E_commerce.exception.DuplicateResourceException;
import com.ecommerce.E_commerce.exception.InsufficientStockException;
import com.ecommerce.E_commerce.exception.InvalidOperationException;
import com.ecommerce.E_commerce.exception.ResourceNotFoundException;
import com.ecommerce.E_commerce.mapper.InventoryMapper;
import com.ecommerce.E_commerce.model.Inventory;
import com.ecommerce.E_commerce.model.Product;
import com.ecommerce.E_commerce.repository.InventoryRepository;
import com.ecommerce.E_commerce.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryMapper inventoryMapper;

    @Autowired
    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                               ProductRepository productRepository,
                               InventoryMapper inventoryMapper) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.inventoryMapper = inventoryMapper;
    }

    @Override
    public InventoryDTO create(InventoryCreateDTO dto) {
        if (inventoryRepository.findByProductId(dto.productId()).isPresent()) {
            throw new DuplicateResourceException("Inventory already exists for product id: " + dto.productId());
        }

        Product product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.productId()));

        Inventory inventory = inventoryMapper.toInventory(dto);
        inventory.setProduct(product);
        inventory.setAvailableQuantity(dto.availableQuantity());
        inventory.setReservedQuantity(dto.reservedQuantity() != null ? dto.reservedQuantity() : 0);
        inventory.setMinimumStockLevel(dto.minimumStockLevel() != null ? dto.minimumStockLevel() : 0);
        inventory.setCreatedAt(Instant.now());
        inventory.setUpdatedAt(Instant.now());

        Inventory savedInventory = inventoryRepository.save(inventory);
        return inventoryMapper.toInventoryDTO(savedInventory);
    }

    @Override
    public InventoryDTO update(Long id, InventoryUpdateDTO dto) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));

        int currentTotal = inventory.getAvailableQuantity() + inventory.getReservedQuantity();
        if (dto.availableQuantity() != null && dto.reservedQuantity() != null) {
            int newTotal = dto.availableQuantity() + dto.reservedQuantity();
            if (newTotal < 0) {
                throw new InvalidOperationException("Total stock (available + reserved) cannot be negative");
            }
        } else if (dto.reservedQuantity() != null) {
            int newReserved = dto.reservedQuantity();
            if (newReserved > currentTotal) {
                throw new InvalidOperationException(
                    String.format("Reserved quantity (%d) cannot exceed total stock (%d)",
                        newReserved, currentTotal));
            }
        } else if (dto.availableQuantity() != null) {
            int newAvailable = dto.availableQuantity();
            int currentReserved = inventory.getReservedQuantity();
            if (newAvailable + currentReserved < 0) {
                throw new InvalidOperationException("Available quantity cannot result in negative total stock");
            }
        }

        inventoryMapper.updateInventoryFromDTO(dto, inventory);
        inventory.setUpdatedAt(Instant.now());

        if (dto.isActive() != null) {
            inventory.setIsActive(dto.isActive());
        }

        Inventory savedInventory = inventoryRepository.save(inventory);
        return inventoryMapper.toInventoryDTO(savedInventory);
    }

    @Override
    public void delete(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));

        inventory.setDeletedAt(Instant.now());
        inventory.setIsActive(false);
        inventory.setUpdatedAt(Instant.now());
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDTO getById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));
        return inventoryMapper.toInventoryDTO(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDTO getByProductId(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + productId));
        return inventoryMapper.toInventoryDTO(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryDTO> findAll(Pageable pageable) {
        Page<Inventory> inventories = inventoryRepository.findAll(pageable);
        return inventories.map(inventoryMapper::toInventoryDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventorySummaryDTO> findAllSummary(Pageable pageable) {
        Page<Inventory> inventories = inventoryRepository.findAll(pageable);
        return inventories.map(inventoryMapper::toInventorySummaryDTO);
    }

    @Override
    public void reserveStock(Long productId, Integer quantity) {
        logger.info("Attempting to reserve stock: productId={}, quantity={}", productId, quantity);
        Inventory inventory = getActiveInventory(productId);

        if (inventory.getAvailableQuantity() < quantity) {
            logger.warn("Insufficient stock for reservation: productId={}, available={}, requested={}", 
                productId, inventory.getAvailableQuantity(), quantity);
            throw new InsufficientStockException(
                String.format("Insufficient stock for product id %d. Available: %d, Requested: %d",
                    productId, inventory.getAvailableQuantity(), quantity));
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventoryRepository.save(inventory);
        logger.info("Stock reserved successfully: productId={}, quantity={}, newAvailable={}, newReserved={}", 
            productId, quantity, inventory.getAvailableQuantity(), inventory.getReservedQuantity());
    }
    @Override
    public void reserveStockBatch(Map<Long, Integer> productQuantities) {
        logger.info("Attempting batch stock reservation for {} items", productQuantities.size());

        List<Long> productIds = productQuantities.keySet().stream().sorted().toList();
        List<Inventory> inventories = inventoryRepository.findByProductIdIn(productIds);

        if (inventories.size() != productIds.size()) {
            Set<Long> foundIds = inventories.stream()
                    .map(inv -> inv.getProduct().getId())
                    .collect(Collectors.toSet());
            List<Long> missingIds = productIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new ResourceNotFoundException("Inventory not found for product ids: " + missingIds);
        }

        for (Inventory inventory : inventories) {
            Long productId = inventory.getProduct().getId();
            Integer requestedQuantity = productQuantities.get(productId);

            if (!inventory.getIsActive()) {
                throw new InvalidOperationException("Product " + productId + " inventory is inactive");
            }

            if (inventory.getAvailableQuantity() < requestedQuantity) {
                throw new InsufficientStockException(
                        String.format("Insufficient stock for product %d. Available: %d, Requested: %d",
                                productId, inventory.getAvailableQuantity(), requestedQuantity));
            }

            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - requestedQuantity);
            inventory.setReservedQuantity(inventory.getReservedQuantity() + requestedQuantity);
            inventory.setUpdatedAt(Instant.now());
        }

        inventoryRepository.saveAll(inventories);
        logger.info("Batch stock reservation successful");
    }
    @Override
    public void releaseStock(Long productId, Integer quantity) {
        logger.info("Attempting to release stock: productId={}, quantity={}", productId, quantity);
        Inventory inventory = getActiveInventory(productId);

        if (inventory.getReservedQuantity() < quantity) {
            logger.warn("Insufficient reserved stock for release: productId={}, reserved={}, requested={}", 
                productId, inventory.getReservedQuantity(), quantity);
            throw new InsufficientStockException(
                String.format("Insufficient reserved stock for product id %d. Reserved: %d, Requested: %d",
                    productId, inventory.getReservedQuantity(), quantity));
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        inventoryRepository.save(inventory);
        logger.info("Stock released successfully: productId={}, quantity={}, newAvailable={}, newReserved={}", 
            productId, quantity, inventory.getAvailableQuantity(), inventory.getReservedQuantity());
    }

    @Override
    public void finalizeReservation(Long productId, Integer quantity) {
        logger.info("Finalizing stock reservation: productId={}, quantity={}", productId, quantity);
        Inventory inventory = getActiveInventory(productId);

        if (inventory.getReservedQuantity() < quantity) {
            logger.warn("Insufficient reserved stock for finalization: productId={}, reserved={}, requested={}", 
                productId, inventory.getReservedQuantity(), quantity);
            throw new InsufficientStockException(
                String.format("Insufficient reserved stock for product id %d. Reserved: %d, Requested: %d",
                    productId, inventory.getReservedQuantity(), quantity));
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        inventoryRepository.save(inventory);
        logger.info("Stock reservation finalized successfully: productId={}, quantity={}, newReserved={}", 
            productId, quantity, inventory.getReservedQuantity());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStockAvailable(Long productId, Integer quantity) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(productId);

        return inventoryOpt
                .filter(Inventory::getIsActive)
                .map(inventory -> inventory.getAvailableQuantity() >= quantity)
                .orElse(false);
    }

    private Inventory getActiveInventory(Long productId) {
        Inventory inventory = inventoryRepository.findByProductIdWithLock(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + productId));

        if (!inventory.getIsActive()) {
            throw new InsufficientStockException("Inventory is not active for product id: " + productId);
        }
        return inventory;
    }
}

