package com.backend.sh.controller;

import com.backend.sh.dto.ApiResponse;
import com.backend.sh.dto.UserDTO;
import com.backend.sh.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        UserDTO user = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByUsername(@PathVariable String username) {
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search users by username or full name")
    public ResponseEntity<ApiResponse<List<UserDTO>>> searchUsers(@RequestParam String query) {
        List<UserDTO> users = userService.searchUsers(query);
        return ResponseEntity.ok(ApiResponse.success(users));
    }
    
    @PutMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @Valid @RequestBody UserDTO.UpdateProfileRequest request) {
        UserDTO user = userService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", user));
    }
    
    @PutMapping("/me/profile-image")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update profile image")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfileImage(@RequestParam String imageUrl) {
        UserDTO user = userService.updateProfileImage(imageUrl);
        return ResponseEntity.ok(ApiResponse.success("Profile image updated successfully", user));
    }
    
    @PutMapping("/me/cover-image")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update cover image")
    public ResponseEntity<ApiResponse<UserDTO>> updateCoverImage(@RequestParam String imageUrl) {
        UserDTO user = userService.updateCoverImage(imageUrl);
        return ResponseEntity.ok(ApiResponse.success("Cover image updated successfully", user));
    }
    
    @PutMapping("/me/password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Change password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody UserDTO.ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }
    
    @PostMapping("/{id}/follow")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Follow a user")
    public ResponseEntity<ApiResponse<UserDTO>> followUser(@PathVariable Long id) {
        UserDTO user = userService.followUser(id);
        return ResponseEntity.ok(ApiResponse.success("User followed successfully", user));
    }
    
    @DeleteMapping("/{id}/unfollow")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Unfollow a user")
    public ResponseEntity<ApiResponse<UserDTO>> unfollowUser(@PathVariable Long id) {
        UserDTO user = userService.unfollowUser(id);
        return ResponseEntity.ok(ApiResponse.success("User unfollowed successfully", user));
    }
    
    @GetMapping("/{id}/followers")
    @Operation(summary = "Get user followers")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getFollowers(@PathVariable Long id) {
        List<UserDTO> followers = userService.getFollowers(id);
        return ResponseEntity.ok(ApiResponse.success(followers));
    }
    
    @GetMapping("/{id}/following")
    @Operation(summary = "Get users that this user follows")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getFollowing(@PathVariable Long id) {
        List<UserDTO> following = userService.getFollowing(id);
        return ResponseEntity.ok(ApiResponse.success(following));
    }
}