package com.whatstheplan.users.controller;

import com.whatstheplan.users.model.entities.User;
import com.whatstheplan.users.model.request.UserCreationRequest;
import com.whatstheplan.users.model.response.UserResponse;
import com.whatstheplan.users.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createNewUserProfile(@Valid @RequestBody UserCreationRequest request) {
        log.info("Creating new user profile with data: {}", request);

        User savedUser = userService.saveUser(request);

        return ResponseEntity.status(CREATED).body(UserResponse.from(savedUser));
    }
}
