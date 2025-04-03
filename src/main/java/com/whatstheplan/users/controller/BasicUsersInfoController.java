package com.whatstheplan.users.controller;

import com.whatstheplan.users.model.response.BasicUserResponse;
import com.whatstheplan.users.services.UserService;
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
public class BasicUsersInfoController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<BasicUserResponse> getUserBasicInfo(@PathVariable UUID userId) {
        log.info("Getting basic user data for user: {}", userId);

        BasicUserResponse response = BasicUserResponse.from(userService.getUserById(userId));

        log.info("Returning successful found user: {}", response);
        return ResponseEntity.ok(response);
    }

}
