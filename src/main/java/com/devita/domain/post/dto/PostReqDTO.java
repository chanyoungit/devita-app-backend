package com.devita.domain.post.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record PostReqDTO(
        String title,
        List<String> imageUrls,
        String description
) {
}