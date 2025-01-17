package com.whatstheplan.users.utils;

import com.whatstheplan.users.exceptions.MissingEmailInTokenException;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

@UtilityClass
public class Utils {

    public static UUID getUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .map(UUID::fromString)
                .orElseThrow(() -> new MissingEmailInTokenException("Invalid token, user id not found.", null));
    }

    public static String getUserEmail() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> ((Jwt) auth.getPrincipal()).getClaimAsString("email"))
                .orElseThrow(() -> new MissingEmailInTokenException("Invalid token, email not found.", null));
    }

}
