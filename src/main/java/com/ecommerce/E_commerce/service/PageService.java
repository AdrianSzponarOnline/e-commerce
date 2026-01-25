package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.page.PageCreateDTO;
import com.ecommerce.E_commerce.dto.page.PageDTO;
import com.ecommerce.E_commerce.dto.page.PageUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PageService {
    PageDTO create(PageCreateDTO dto);
    
    PageDTO update(Long id, PageUpdateDTO dto);
    
    void delete(Long id);
    
    PageDTO getById(Long id);
    
    PageDTO getBySlug(String slug);
    
    List<PageDTO> getAll();
    
    List<PageDTO> getAllActive();
    
    Page<PageDTO> searchBySlug(String slug, Pageable pageable);
    
    Page<PageDTO> searchByTitle(String title, Pageable pageable);
    
    Page<PageDTO> search(String slug, String title, Pageable pageable);
}
