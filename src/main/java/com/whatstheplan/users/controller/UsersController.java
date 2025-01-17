package com.whatstheplan.users.controller;

import com.whatstheplan.users.model.entities.User;
import com.whatstheplan.users.model.request.UserProfileRequest;
import com.whatstheplan.users.model.response.UserResponse;
import com.whatstheplan.users.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.whatstheplan.users.utils.Utils.getUserEmail;
import static com.whatstheplan.users.utils.Utils.getUserId;
import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserResponse> retrieveUserProfile() {
        log.info("Getting user data for user: {}, email: {}", getUserId(), getUserEmail());

        User savedUser = userService.getUserById(getUserId());

        return ResponseEntity.ok(UserResponse.from(savedUser));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createNewUserProfile(@Valid @RequestBody UserProfileRequest request) {
        log.info("Creating new user profile with data: {}", request);

        User savedUser = userService.saveUser(request);

        return ResponseEntity.status(CREATED).body(UserResponse.from(savedUser));
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateUserProfile(@Valid @RequestBody UserProfileRequest request) {
        log.info("Updating user profile with data: {}", request);

        User updatedUser = userService.updateUser(request);

        return ResponseEntity.ok(UserResponse.from(updatedUser));
    }
}
