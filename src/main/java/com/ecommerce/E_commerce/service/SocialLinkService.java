package com.ecommerce.E_commerce.service;

import com.ecommerce.E_commerce.dto.sociallink.SocialLinkCreateDTO;
import com.ecommerce.E_commerce.dto.sociallink.SocialLinkDTO;
import com.ecommerce.E_commerce.dto.sociallink.SocialLinkUpdateDTO;

import java.util.List;

public interface SocialLinkService {
    SocialLinkDTO create(SocialLinkCreateDTO dto);
    
    SocialLinkDTO update(Long id, SocialLinkUpdateDTO dto);
    
    void delete(Long id);
    
    SocialLinkDTO getById(Long id);
    
    List<SocialLinkDTO> getAll();
    
    List<SocialLinkDTO> getAllActive();
}
