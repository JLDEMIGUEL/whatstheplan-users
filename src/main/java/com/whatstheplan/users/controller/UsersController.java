package com.whatstheplan.users.controller;

import com.whatstheplan.users.model.entities.User;
import com.whatstheplan.users.model.request.UserProfileRequest;
import com.whatstheplan.users.model.response.UserResponse;
import com.whatstheplan.users.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin(originPatterns = "*")
@Tag(name = "User Profile", description = "Operations to manage user profile")
public class UsersController {

    private final UserService userService;

    @Operation(summary = "Retrieve the profile of the authenticated user",
            description = "Returns the profile information of the currently authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated", content = @Content)
    })
    @GetMapping
    public ResponseEntity<UserResponse> retrieveUserProfile() {
        log.info("Getting user data for user: {}, email: {}", getUserId(), getUserEmail());

        User savedUser = userService.getUserById(getUserId());

        log.info("Returning successful found user: {}", getUserId());
        return ResponseEntity.ok(UserResponse.from(savedUser));
    }

    @Operation(summary = "Create a new user profile",
            description = "Creates a new user profile with the provided information.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User profile created successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated", content = @Content)
    })
    @PostMapping
    public ResponseEntity<UserResponse> createNewUserProfile(
            @Parameter(description = "User profile creation request", required = true)
            @Valid @RequestBody UserProfileRequest request) {
        log.info("Creating new user profile with data: {}", request);

        User savedUser = userService.saveUser(request);

        log.info("Returning successful created user: {}", getUserId());
        return ResponseEntity.status(CREATED).body(UserResponse.from(savedUser));
    }

    @Operation(summary = "Update the authenticated user's profile",
            description = "Updates the user profile with the provided new data.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated", content = @Content)
    })
    @PutMapping
    public ResponseEntity<UserResponse> updateUserProfile(
            @Parameter(description = "User profile update request", required = true)
            @Valid @RequestBody UserProfileRequest request) {
        log.info("Updating user profile with data: {}", request);

        User updatedUser = userService.updateUser(request);

        log.info("Returning successful updated user: {}", getUserId());
        return ResponseEntity.ok(UserResponse.from(updatedUser));
    }
}
