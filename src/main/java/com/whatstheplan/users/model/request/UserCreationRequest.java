package com.whatstheplan.users.model.request;


import com.whatstheplan.users.model.ActivityType;
import com.whatstheplan.users.model.entities.Preferences;
import com.whatstheplan.users.model.entities.User;
import jakarta.validation.constraints.NotBlank;
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
