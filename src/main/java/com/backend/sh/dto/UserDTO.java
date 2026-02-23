package com.backend.sh.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String bio;
    private String location;
    private String profileImageUrl;
    private String coverImageUrl;
    private String website;
    private Boolean verified;
    private Boolean active;
    private LocalDateTime createdAt;
    
    // Stats
    private Integer followersCount;
    private Integer followingCount;
    private Integer postsCount;
    
    // For viewing other users
    private Boolean isFollowing;
    private Boolean isFollowedBy;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProfileRequest {
        @Size(max = 100, message = "Full name must not exceed 100 characters")
        private String fullName;
        
        @Size(max = 500, message = "Bio must not exceed 500 characters")
        private String bio;
        
        @Size(max = 100, message = "Location must not exceed 100 characters")
        private String location;
        
        @Size(max = 255, message = "Website must not exceed 255 characters")
        private String website;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
    }
}