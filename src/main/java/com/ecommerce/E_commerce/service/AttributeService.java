package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.attribute.AttributeCreateDTO;
import com.ecommerce.E_commerce.dto.attribute.AttributeDTO;
import com.ecommerce.E_commerce.dto.attribute.AttributeUpdateDTO;
import com.ecommerce.E_commerce.model.Attribute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface AttributeService {
    public Attribute findOrCreateAttribute(AttributeCreateDTO dto);
    AttributeDTO createAttribute(AttributeCreateDTO dto);
    public AttributeDTO update(Long id, AttributeUpdateDTO dto);
    public void deleteAttribute(Long id);
    AttributeDTO getAttributeById(Long id);
    Page<AttributeDTO> getActiveAttributes(Pageable pageable);
    Page<AttributeDTO> getInactiveAttributes(Pageable pageable);
    void restoreAttribute(Long id);
    List<String> getAllAttributeNames();
}
