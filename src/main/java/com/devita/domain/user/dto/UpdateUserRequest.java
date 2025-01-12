package com.devita.domain.user.dto;

public record UpdateUserRequest(
        String nickname,
        String profileImage
) {}