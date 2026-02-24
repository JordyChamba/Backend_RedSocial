package com.backend.sh.service;

import com.backend.sh.dto.UserDTO;
import com.backend.sh.entity.Notification;
import com.backend.sh.entity.User;
import com.backend.sh.exception.BadRequestException;
import com.backend.sh.exception.ResourceNotFoundException;
import com.backend.sh.repository.PostRepository;
import com.backend.sh.repository.UserRepository;
import com.backend.sh.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser() {
        Long userId = CurrentUser.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return convertToDTO(user, null);
    }
    
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Long currentUserId = null;
        try {
            currentUserId = CurrentUser.getUserId();
        } catch (Exception e) {
            // User is not authenticated, that's ok
        }
        
        return convertToDTO(user, currentUserId);
    }
    
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        Long currentUserId = null;
        try {
            currentUserId = CurrentUser.getUserId();
        } catch (Exception e) {
            // User is not authenticated, that's ok
        }
        
        return convertToDTO(user, currentUserId);
    }
    
    @Transactional(readOnly = true)
    public List<UserDTO> searchUsers(String query) {
        List<User> users = userRepository.searchUsers(query);
        
        Long currentUserId = null;
        try {
            currentUserId = CurrentUser.getUserId();
        } catch (Exception e) {
            // User is not authenticated, that's ok
        }
        
        Long finalCurrentUserId = currentUserId;
        return users.stream()
                .map(user -> convertToDTO(user, finalCurrentUserId))
                .collect(Collectors.toList());
    }
    
    @Transactional
    public UserDTO updateProfile(UserDTO.UpdateProfileRequest request) {
        Long userId = CurrentUser.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Update fields
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }
        if (request.getWebsite() != null) {
            user.setWebsite(request.getWebsite());
        }
        
        user = userRepository.save(user);
        
        return convertToDTO(user, userId);
    }
    
    @Transactional
    public UserDTO updateProfileImage(String imageUrl) {
        Long userId = CurrentUser.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.setProfileImageUrl(imageUrl);
        user = userRepository.save(user);
        
        return convertToDTO(user, userId);
    }
    
    @Transactional
    public UserDTO updateCoverImage(String imageUrl) {
        Long userId = CurrentUser.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.setCoverImageUrl(imageUrl);
        user = userRepository.save(user);
        
        return convertToDTO(user, userId);
    }
    
    @Transactional
    public void changePassword(UserDTO.ChangePasswordRequest request) {
        Long userId = CurrentUser.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    
    @Transactional
    public UserDTO followUser(Long userIdToFollow) {
        Long currentUserId = CurrentUser.getUserId();
        
        if (currentUserId.equals(userIdToFollow)) {
            throw new BadRequestException("You cannot follow yourself");
        }
        
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        
        User userToFollow = userRepository.findById(userIdToFollow)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userIdToFollow));
        
        if (currentUser.isFollowing(userToFollow)) {
            throw new BadRequestException("You are already following this user");
        }
        
        currentUser.follow(userToFollow);
        userRepository.save(currentUser);
        
        // Create notification
        notificationService.createFollowNotification(currentUser, userToFollow);
        
        return convertToDTO(userToFollow, currentUserId);
    }
    
    @Transactional
    public UserDTO unfollowUser(Long userIdToUnfollow) {
        Long currentUserId = CurrentUser.getUserId();
        
        if (currentUserId.equals(userIdToUnfollow)) {
            throw new BadRequestException("You cannot unfollow yourself");
        }
        
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        
        User userToUnfollow = userRepository.findById(userIdToUnfollow)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userIdToUnfollow));
        
        if (!currentUser.isFollowing(userToUnfollow)) {
            throw new BadRequestException("You are not following this user");
        }
        
        currentUser.unfollow(userToUnfollow);
        userRepository.save(currentUser);
        
        return convertToDTO(userToUnfollow, currentUserId);
    }
    
    @Transactional(readOnly = true)
    public List<UserDTO> getFollowers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Long currentUserId = null;
        try {
            currentUserId = CurrentUser.getUserId();
        } catch (Exception e) {
            // User is not authenticated, that's ok
        }
        
        Long finalCurrentUserId = currentUserId;
        return user.getFollowers().stream()
                .map(follower -> convertToDTO(follower, finalCurrentUserId))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserDTO> getFollowing(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Long currentUserId = null;
        try {
            currentUserId = CurrentUser.getUserId();
        } catch (Exception e) {
            // User is not authenticated, that's ok
        }
        
        Long finalCurrentUserId = currentUserId;
        return user.getFollowing().stream()
                .map(following -> convertToDTO(following, finalCurrentUserId))
                .collect(Collectors.toList());
    }
    
    private UserDTO convertToDTO(User user, Long currentUserId) {
        UserDTO dto = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .bio(user.getBio())
                .location(user.getLocation())
                .profileImageUrl(user.getProfileImageUrl())
                .coverImageUrl(user.getCoverImageUrl())
                .website(user.getWebsite())
                .verified(user.getVerified())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .followersCount(user.getFollowers() != null ? user.getFollowers().size() : 0)
                .followingCount(user.getFollowing() != null ? user.getFollowing().size() : 0)
                .postsCount(postRepository.countByAuthorId(user.getId()).intValue())
                .build();
        
        // Add relationship info if current user is viewing
        if (currentUserId != null && !currentUserId.equals(user.getId())) {
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            if (currentUser != null) {
                dto.setIsFollowing(currentUser.isFollowing(user));
                dto.setIsFollowedBy(user.isFollowing(currentUser));
            }
        }
        
        return dto;
    }
}
