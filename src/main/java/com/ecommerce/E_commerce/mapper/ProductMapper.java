package com.ecommerce.E_commerce.mapper;

import com.ecommerce.E_commerce.dto.product.*;
import com.ecommerce.E_commerce.model.Product;
import com.ecommerce.E_commerce.model.ProductAttributeValue;
import com.ecommerce.E_commerce.service.ImageUrlService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        uses = {CategoryMapper.class, ProductAttributeValueMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class ProductMapper {

    @Autowired
    protected ImageUrlService imageUrlService;

    @Mapping(target = "category", source = "category", qualifiedByName = "categoryFlat")
    @Mapping(target = "attributeValues", source = "attributeValues")
    @Mapping(target = "thumbnailUrl", source = "thumbnailUrl", qualifiedByName = "buildFullUrl")
    public abstract ProductDTO toProductDTO(Product product);

    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(target = "thumbnailUrl", source = "thumbnailUrl", qualifiedByName = "buildFullUrl")
    public abstract ProductSummaryDTO toProductSummaryDTO(Product product);

    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "attributes", source = "attributeValues", qualifiedByName = "mapAttributesToMap")
    @Mapping(target = "thumbnailUrl", source = "thumbnailUrl", qualifiedByName = "buildFullUrl")
    public abstract ProductSearchDTO toDTO(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "attributeValues", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    public abstract Product toProduct(ProductCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "attributeValues", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    public abstract void updateProductFromDTO(ProductUpdateDTO dto, @MappingTarget Product product);

    @Named("buildFullUrl")
    public String buildFullUrl(String relativeUrl) {
        if (imageUrlService == null) {
            return relativeUrl;
        }
        return imageUrlService.buildFullUrl(relativeUrl);
    }

    @Named("mapAttributesToMap")
    public Map<String, String> mapAttributesToMap(List<ProductAttributeValue> attributeValues) {
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
