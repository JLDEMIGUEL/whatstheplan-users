package com.whatstheplan.users.model.response;

import com.whatstheplan.users.model.entities.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Basic user information response")
public class BasicUserResponse {

    @Schema(description = "Username of the user", example = "john_doe")
    private String username;

    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    private String email;

    public static BasicUserResponse from(User user) {
        return BasicUserResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}

