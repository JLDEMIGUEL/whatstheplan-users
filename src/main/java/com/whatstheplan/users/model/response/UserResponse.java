package com.whatstheplan.users.model.response;

import com.whatstheplan.users.model.ActivityType;
import com.whatstheplan.users.model.entities.Preferences;
import com.whatstheplan.users.model.entities.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed user profile response")
public class UserResponse {

    @Schema(description = "Username of the user", example = "john_doe")
    private String username;

    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User's first name", example = "John")
    private String firstName;

    @Schema(description = "User's last name", example = "Doe")
    private String lastName;

    @Schema(description = "City where the user lives", example = "Madrid")
    private String city;

    @Schema(description = "List of user's activity type preferences", example = "[\"sports\", \"music\"]")
    private List<String> preferences;

    public static UserResponse from(User user) {
        List<String> preferences = user.getPreferences().stream()
                .map(Preferences::getActivityType)
                .map(ActivityType::getName)
                .toList();

        return UserResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .city(user.getCity())
                .preferences(preferences)
                .build();
    }
}
