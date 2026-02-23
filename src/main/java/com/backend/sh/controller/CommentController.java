package com.backend.sh.controller;

import com.backend.sh.dto.ApiResponse;
import com.backend.sh.dto.CommentDTO;
import com.backend.sh.service.CommentService;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Comment management endpoints")
public class CommentController {
    
    private final CommentService commentService;
    
    @PostMapping("/posts/{postId}/comments")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a comment on a post")
    public ResponseEntity<ApiResponse<CommentDTO>> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentDTO.CreateCommentRequest request) {
        CommentDTO comment = commentService.createComment(postId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment created successfully", comment));
    }
    
    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "Get comments for a post (top-level only, paginated)")
    public ResponseEntity<ApiResponse<Page<CommentDTO>>> getCommentsByPostId(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CommentDTO> comments = commentService.getCommentsByPostId(postId, page, size);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }
    
    @GetMapping("/comments/{commentId}/replies")
    @Operation(summary = "Get replies for a comment")
    public ResponseEntity<ApiResponse<List<CommentDTO>>> getRepliesByCommentId(
            @PathVariable Long commentId) {
        List<CommentDTO> replies = commentService.getRepliesByCommentId(commentId);
        return ResponseEntity.ok(ApiResponse.success(replies));
    }
    
    @PutMapping("/comments/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a comment")
    public ResponseEntity<ApiResponse<CommentDTO>> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentDTO.UpdateCommentRequest request) {
        CommentDTO comment = commentService.updateComment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Comment updated successfully", comment));
    }
    
    @DeleteMapping("/comments/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
    }
}