package com.devita.domain.post.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record PostsResDTO(
        Long id,
        String title,
        String description,
        List<String> imageUrls,
        Long likes,
        Long views
) {
}
