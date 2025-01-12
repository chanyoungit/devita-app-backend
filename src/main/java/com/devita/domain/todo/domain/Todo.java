package com.devita.domain.todo.domain;

import com.devita.common.entity.BaseEntity;
import com.devita.domain.category.domain.Category;
import com.devita.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class Todo extends BaseEntity {
    @Id
    @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String missionCategory;

    private String title;
    private Boolean status;
    private LocalDate date;
    private Boolean isDone;

    @Builder
    private Todo(User user, Category category, String missionCategory, String title, Boolean status, LocalDate date) {
        this.user = user;
        this.category = category;
        this.missionCategory = missionCategory;
        this.title = title;
        this.status = status != null ? status : false;
        this.date = date;
        this.isDone = false;
    }

    public void toggleStatus() {
        this.status = (this.status == null || !this.status);
    }

    public void isDone() {
        this.isDone = true;
    }

    public void updateDetails(Category category, String title, LocalDate date) {
        this.category = category;
        this.title = title;
        this.date = date;
    }
}