package com.ecommerce.E_commerce.mapper;

import com.ecommerce.E_commerce.dto.product.ProductCreateDTO;
import com.ecommerce.E_commerce.dto.product.ProductDTO;
import com.ecommerce.E_commerce.dto.product.ProductSummaryDTO;
import com.ecommerce.E_commerce.dto.product.ProductUpdateDTO;
import com.ecommerce.E_commerce.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {CategoryMapper.class, ProductAttributeValueMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {
    
    @Mapping(target = "category", source = "category")
    @Mapping(target = "attributeValues", source = "attributeValues")
    ProductDTO toProductDTO(Product product);

    @Mapping(source = "category.name", target = "categoryName")
    ProductSummaryDTO toProductSummaryDTO(Product product);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "attributeValues", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    Product toProduct(ProductCreateDTO dto);
    

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "attributeValues", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateProductFromDTO(ProductUpdateDTO dto, @MappingTarget Product product);
}
