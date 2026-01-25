package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.faq.FaqItemCreateDTO;
import com.ecommerce.E_commerce.dto.faq.FaqItemDTO;
import com.ecommerce.E_commerce.dto.faq.FaqItemUpdateDTO;

import java.util.List;

public interface FaqService {
    FaqItemDTO create(FaqItemCreateDTO dto);
    
    FaqItemDTO update(Long id, FaqItemUpdateDTO dto);
    
    void delete(Long id);
    
    FaqItemDTO getById(Long id);
    
    List<FaqItemDTO> getAll();
    
    List<FaqItemDTO> getAllActive();
}
