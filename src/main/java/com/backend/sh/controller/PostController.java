package com.backend.sh.controller;

import com.backend.sh.dto.ApiResponse;
import com.backend.sh.dto.PostDTO;
import com.backend.sh.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Post management endpoints")
public class PostController {
    
    private final PostService postService;
    
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new post")
    public ResponseEntity<ApiResponse<PostDTO>> createPost(
            @Valid @RequestBody PostDTO.CreatePostRequest request) {
        PostDTO post = postService.createPost(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post created successfully", post));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get post by ID")
    public ResponseEntity<ApiResponse<PostDTO>> getPostById(@PathVariable Long id) {
        PostDTO post = postService.getPostById(id);
        return ResponseEntity.ok(ApiResponse.success(post));
    }
    
    @GetMapping
    @Operation(summary = "Get all posts (paginated)")
    public ResponseEntity<ApiResponse<Page<PostDTO>>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PostDTO> posts = postService.getAllPosts(page, size);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get posts by user ID")
    public ResponseEntity<ApiResponse<Page<PostDTO>>> getPostsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PostDTO> posts = postService.getPostsByUserId(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }
    
    @GetMapping("/feed")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get personalized feed (posts from followed users)")
    public ResponseEntity<ApiResponse<Page<PostDTO>>> getFeedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PostDTO> posts = postService.getFeedPosts(page, size);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }
    
    @GetMapping("/trending")
    @Operation(summary = "Get trending posts (most liked in last 24 hours)")
    public ResponseEntity<ApiResponse<List<PostDTO>>> getTrendingPosts(
            @RequestParam(defaultValue = "10") int limit) {
        List<PostDTO> posts = postService.getTrendingPosts(limit);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search posts by content")
    public ResponseEntity<ApiResponse<Page<PostDTO>>> searchPosts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PostDTO> posts = postService.searchPosts(query, page, size);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }
    
    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a post")
    public ResponseEntity<ApiResponse<PostDTO>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostDTO.UpdatePostRequest request) {
        PostDTO post = postService.updatePost(id, request);
        return ResponseEntity.ok(ApiResponse.success("Post updated successfully", post));
    }
    
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a post")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok(ApiResponse.success("Post deleted successfully", null));
    }
    
    @PostMapping("/{id}/like")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Like a post")
    public ResponseEntity<ApiResponse<PostDTO>> likePost(@PathVariable Long id) {
        PostDTO post = postService.likePost(id);
        return ResponseEntity.ok(ApiResponse.success("Post liked successfully", post));
    }
    
    @DeleteMapping("/{id}/unlike")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Unlike a post")
    public ResponseEntity<ApiResponse<PostDTO>> unlikePost(@PathVariable Long id) {
        PostDTO post = postService.unlikePost(id);
        return ResponseEntity.ok(ApiResponse.success("Post unliked successfully", post));
    }
}