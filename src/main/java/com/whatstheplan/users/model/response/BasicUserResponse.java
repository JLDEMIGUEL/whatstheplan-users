package com.whatstheplan.users.model.response;

import com.whatstheplan.users.model.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasicUserResponse {

    private String username;

    public static BasicUserResponse from(User user) {
        return BasicUserResponse.builder()
                .username(user.getUsername())
                .build();
    }
}
