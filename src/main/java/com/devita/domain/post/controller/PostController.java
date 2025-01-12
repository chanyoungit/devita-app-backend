package com.devita.domain.post.controller;

import com.devita.common.response.ApiResponse;
import com.devita.domain.post.dto.*;
import com.devita.domain.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PostController {

    private final PostService postService;

    // 게시물 생성
    @PostMapping("/post")
    public ApiResponse<Long> addPost(@AuthenticationPrincipal Long userId, @RequestBody PostReqDTO postReqDTO) {
        Long postId = postService.addPost(userId, postReqDTO).getId();

        return ApiResponse.success(postId);
    }

    // 게시물 삭제
    @DeleteMapping("/post/{postId}")
    public ApiResponse<List<String>> deletePost(@AuthenticationPrincipal Long userId, @PathVariable Long postId) {
        List<String> imageUrls = postService.deletePost(userId, postId);

        return ApiResponse.success(imageUrls);
    }

    // 게시물 수정
    @PutMapping("/post/{postId}")
    public ApiResponse<PostResDTO> updatePost(@AuthenticationPrincipal Long userId, @PathVariable Long postId, @RequestBody PostReqDTO postReqDTO) {
        PostResDTO postResDTO = postService.updatePost(userId, postId, postReqDTO);

        return ApiResponse.success(postResDTO);
    }

    // 게시물 상세 조회
    @GetMapping("/post/{postId}")
    public ApiResponse<PostResDTO> getPost(@AuthenticationPrincipal Long userId, @PathVariable Long postId) {
        PostResDTO postResDTO = postService.getPost(userId, postId);

        return ApiResponse.success(postResDTO);
    }

    // 작성한 게시물 조회
    @GetMapping("/posts/my")
    public ApiResponse<List<PostsResDTO>> getMyPosts(@AuthenticationPrincipal Long userId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        List<PostsResDTO> posts = postService.getMyPosts(userId, page, size);

        return ApiResponse.success(posts);
    }

    @PostMapping("/post/{postId}/like")
    public ApiResponse<Long> increaseLikesRedis(@AuthenticationPrincipal Long userId, @PathVariable Long postId) {
        Long likes = postService.likePost(userId, postId);
        return ApiResponse.success(likes);
    }

    @PostMapping("/post/{postId}/unlike")
    public ApiResponse<Long> decreaseLikesRedis(@AuthenticationPrincipal Long userId, @PathVariable Long postId) {
        Long likes = postService.unlikePost(userId, postId);
        return ApiResponse.success(likes);
    }

    @Operation(summary = "팔로우한 사용자들의 게시물 조회")
    @GetMapping("/feed")
    public ApiResponse<Page<FollowingPostResponseDTO>> getFollowingUsersPosts(@AuthenticationPrincipal Long userId, @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.success(postService.getFollowingUsersPosts(userId, pageable));
    }

    @Operation(summary = "전체 게시물 페이징 조회")
    @GetMapping("/posts/all")
    public ApiResponse<Page<AllPostsResDTO>> getAllPosts(@AuthenticationPrincipal Long userId, @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(postService.getAllPosts(userId, pageable));
    }
}
