package com.whatstheplan.users.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UsersController {

    @GetMapping("/test")
    @PreAuthorize("hasRole('user')")
    public ResponseEntity<Map<String, Object>> test() {
        log.info("Test endpoint requested");
        return ResponseEntity.ok().body(Map.of("message", "Test endpoint requested"));
    }
}
