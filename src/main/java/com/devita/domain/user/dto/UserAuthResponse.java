package com.devita.domain.user.dto;

import com.devita.domain.category.dto.CategoryResDTO;
import lombok.Builder;

import java.util.List;

@Builder
public record UserAuthResponse(
        String refreshToken,
        String accessToken,
        String kakaoAccessToken,
        String email,
        String nickname,
        String imageUrl,
        List<CategoryResDTO> categories
) {}