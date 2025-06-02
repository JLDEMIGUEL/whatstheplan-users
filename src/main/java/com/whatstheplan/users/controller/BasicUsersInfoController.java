package com.whatstheplan.users.controller;

import com.whatstheplan.users.model.response.BasicUserResponse;
import com.whatstheplan.users.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/users-info")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
@Tag(name = "Basic User Info", description = "Retrieve basic information about users")
public class BasicUsersInfoController {

    private final UserService userService;

    @Operation(summary = "Get basic user information by user ID",
            description = "Returns basic user details such as username, name, and other public profile info.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User information retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BasicUserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid user ID supplied",
                    content = @Content)
    })
    @GetMapping("/{userId}")
    public ResponseEntity<BasicUserResponse> getUserBasicInfo(
            @Parameter(description = "UUID of the user to retrieve", required = true)
            @PathVariable UUID userId) {

        log.info("Getting basic user data for user: {}", userId);

        BasicUserResponse response = BasicUserResponse.from(userService.getUserById(userId));

        log.info("Returning successful found user: {}", response);
        return ResponseEntity.ok(response);
    }

}
