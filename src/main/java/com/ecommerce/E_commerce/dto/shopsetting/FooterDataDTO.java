package com.ecommerce.E_commerce.dto.shopsetting;

import com.ecommerce.E_commerce.dto.sociallink.SocialLinkDTO;

import java.util.List;

public record FooterDataDTO(
        String shopName,
        String logoUrl,
        String footerCopyright,
        ContactInfoDTO contact,
        List<SocialLinkDTO> socialLinks
) {
    public record ContactInfoDTO(
            String phone,
            String email,
            String address,
            String openingHours
    ) {
    }
}
