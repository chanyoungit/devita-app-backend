package com.devita.domain.post.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AllPostsResDTO(
        Long id,
        String title,
        String description,
        Long writerId,
        String writerNickname,
        String writerImageUrl,
        Long likes,
        Long views,
        List<String> images,
        LocalDateTime createdAt,
        boolean isLiked,
        boolean isFollowed
) {}
