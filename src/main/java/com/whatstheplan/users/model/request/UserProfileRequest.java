package com.whatstheplan.users.model.request;


import com.whatstheplan.users.model.ActivityType;
import com.whatstheplan.users.model.entities.Preferences;
import com.whatstheplan.users.model.entities.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.whatstheplan.users.utils.Utils.getUserEmail;
import static com.whatstheplan.users.utils.Utils.getUserId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request model for creating or updating a user profile")
public class UserProfileRequest {

    @NotBlank(message = "Username is mandatory.")
    @Size(max = 255, message = "Username must be less than or equal to 255 characters.")
    @Pattern(
            regexp = "^[a-zA-Z0-9._-]{3,15}$",
            message = "Username must be 3-15 characters long and can only include letters, numbers, dots, underscores, and hyphens."
    )
    @Schema(
            description = "Unique username, 3-15 characters; letters, numbers, dot, underscore, hyphen allowed",
            example = "john.doe_99",
            maxLength = 255,
            minLength = 3,
            pattern = "^[a-zA-Z0-9._-]{3,15}$",
            required = true
    )
    private String username;

    @NotBlank(message = "First name is mandatory.")
    @Size(max = 255, message = "First name must be less than or equal to 255 characters.")
    @Schema(description = "User's first name", example = "John", maxLength = 255, required = true)
    private String firstName;

    @NotBlank(message = "Last name is mandatory.")
    @Size(max = 255, message = "Last name must be less than or equal to 255 characters.")
    @Schema(description = "User's last name", example = "Doe", maxLength = 255, required = true)
    private String lastName;

    @NotBlank(message = "City name is mandatory.")
    @Size(max = 255, message = "City must be less than or equal to 255 characters.")
    @Schema(description = "City where the user lives", example = "Madrid", maxLength = 255, required = true)
    private String city;

    @Size(message = "Preferences cannot be empty.")
    @Schema(description = "List of user preferences or interests", example = "[\"sports\", \"music\"]")
    private List<@NotBlank(message = "Each preference must not be blank.") String> preferences;

    public User toEntity() {
        User userEntity = User.builder()
                .id(getUserId())
                .email(getUserEmail())
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .city(city)
                .build();

        List<Preferences> preferencesEntities = this.preferences.stream()
                .map(ActivityType::from)
                .map(at -> new Preferences(null, at, userEntity))
                .toList();

        userEntity.setPreferences(preferencesEntities);

        return userEntity;
    }
}
