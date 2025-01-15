package com.whatstheplan.users.model.request;


import com.whatstheplan.users.exceptions.MissingEmailInTokenException;
import com.whatstheplan.users.model.ActivityType;
import com.whatstheplan.users.model.entities.Preferences;
import com.whatstheplan.users.model.entities.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationRequest {
    @NotBlank(message = "Username is mandatory.")
    @Size(max = 255, message = "Username must be less than or equal to 255 characters.")
    private String username;

    @NotBlank(message = "First name is mandatory.")
    @Size(max = 255, message = "First name must be less than or equal to 255 characters.")
    private String firstName;

    @NotBlank(message = "Last name is mandatory.")
    @Size(max = 255, message = "Last name must be less than or equal to 255 characters.")
    private String lastName;

    @NotBlank(message = "City name is mandatory.")
    @Size(max = 255, message = "City must be less than or equal to 255 characters.")
    private String city;

    @Size(message = "Preferences cannot be empty.")
    private List<@NotBlank(message = "Each preference must not be blank.") String> preferences;

    public User toEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = Optional.ofNullable(((Jwt) authentication.getPrincipal()).getClaimAsString("email"))
                .orElseThrow(() -> new MissingEmailInTokenException("Invalid token, email not found.", null));
        User userEntity = User.builder()
                .id(UUID.fromString(authentication.getName()))
                .email(email)
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
