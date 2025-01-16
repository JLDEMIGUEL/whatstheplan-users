package com.whatstheplan.users.model.response;

import com.whatstheplan.users.model.ActivityType;
import com.whatstheplan.users.model.entities.Preferences;
import com.whatstheplan.users.model.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String city;
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
