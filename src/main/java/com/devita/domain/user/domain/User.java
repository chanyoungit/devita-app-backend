package com.devita.domain.user.domain;

import com.devita.common.entity.BaseEntity;
import com.devita.domain.category.domain.Category;
import com.devita.domain.post.domain.Post;
import com.devita.domain.todo.domain.Todo;
import com.devita.domain.follow.domain.Follow;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    @Setter
    private Long id;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AuthProvider provider;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Todo> todoEntities;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> categories;

    @OneToMany(mappedBy = "writer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts;

    @ElementCollection
    @CollectionTable(
            name = "user_preferred_categories",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "category_name")
    private List<PreferredCategory> preferredCategories = new ArrayList<>();

    private String profileImage;

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followings = new ArrayList<>();  // 내가 팔로우하는 사람들

    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followers = new ArrayList<>();   // 나를 팔로우하는 사람들

    public void follow(User targetUser) {
        Follow follow = Follow.builder()
                .follower(this)
                .following(targetUser)
                .build();
        this.followings.add(follow);
    }

    public void unfollow(User targetUser) {
        this.followings.removeIf(follow ->
                follow.getFollowing().getId().equals(targetUser.getId()));
    }

    @Builder
    public User(String email, String nickname, AuthProvider provider, String profileImage) {
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
        this.profileImage = profileImage;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void updatePreferredCategories(List<PreferredCategory> categories) {
        this.preferredCategories.clear();
        this.preferredCategories.addAll(categories);
    }
}