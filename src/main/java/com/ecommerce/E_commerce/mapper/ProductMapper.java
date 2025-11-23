package com.ecommerce.E_commerce.mapper;

import com.ecommerce.E_commerce.dto.product.*;
import com.ecommerce.E_commerce.model.Product;
import com.ecommerce.E_commerce.model.ProductAttributeValue;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        uses = {CategoryMapper.class, ProductAttributeValueMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {
    
    @Mapping(target = "category", source = "category", qualifiedByName = "categoryFlat")
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

    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "attributes", source = "attributeValues", qualifiedByName = "mapAttributesToMap")
    ProductSearchDTO toDTO(Product product);
    

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "attributeValues", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateProductFromDTO(ProductUpdateDTO dto, @MappingTarget Product product);

    @Named("mapAttributesToMap")
    default Map<String, String> mapAttributesToMap(List<ProductAttributeValue> attributeValues) {
        if (attributeValues == null) {
            return Collections.emptyMap();
        }

        return attributeValues.stream()
                .filter(val -> val.getAttribute() != null && val.getAttribute().getName() != null && val.getValue() != null)
                .collect(Collectors.toMap(
                        val -> val.getAttribute().getName(),
                        ProductAttributeValue::getValue,
                        (existing, replacement) -> existing));
    }
}
