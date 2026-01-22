package com.ecommerce.E_commerce.mapper;

import com.ecommerce.E_commerce.dto.inventory.InventoryCreateDTO;
import com.ecommerce.E_commerce.dto.inventory.InventoryDTO;
import com.ecommerce.E_commerce.dto.inventory.InventorySummaryDTO;
import com.ecommerce.E_commerce.dto.inventory.InventoryUpdateDTO;
import com.ecommerce.E_commerce.model.Inventory;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InventoryMapper {
    
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "thumbnailUrl", source = "product.thumbnailUrl")
    InventoryDTO toInventoryDTO(Inventory inventory);
    
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "sku", source = "product.sku")
    @Mapping(target = "thumbnailUrl", source = "product.thumbnailUrl")
    @Mapping(target = "belowMinimum", expression = "java(inventory.getAvailableQuantity() < inventory.getMinimumStockLevel())")
    InventorySummaryDTO toInventorySummaryDTO(Inventory inventory);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    Inventory toInventory(InventoryCreateDTO dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateInventoryFromDTO(InventoryUpdateDTO dto, @MappingTarget Inventory inventory);
}

