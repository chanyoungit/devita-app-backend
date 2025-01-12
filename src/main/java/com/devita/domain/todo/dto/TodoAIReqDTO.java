package com.devita.domain.todo.dto;

import java.time.LocalDate;

public record TodoAIReqDTO(
        String title, //Java 공부하기
        LocalDate date, // 2024-12-02
        String missionType, // 일일 미션, 자율 미션
        String missionCategory, // Java, Python
        Long userId // 1
) {}