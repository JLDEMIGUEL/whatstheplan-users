package com.whatstheplan.users.services;

import com.whatstheplan.users.exceptions.EmailAlreadyExistsException;
import com.whatstheplan.users.exceptions.UserNotExistsException;
import com.whatstheplan.users.exceptions.UsernameAlreadyExistsException;
import com.whatstheplan.users.model.entities.User;
import com.whatstheplan.users.model.request.UserProfileRequest;
import com.whatstheplan.users.repository.PreferencesRepository;
import com.whatstheplan.users.repository.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.whatstheplan.users.utils.Utils.getUserId;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;
    private final PreferencesRepository preferencesRepository;

    public User getUserById(UUID userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new UserNotExistsException("User not found with userId: " + userId, null));
    }

    @Transactional
    public User saveUser(UserProfileRequest request) {
        try {
            log.info("Saving into database user with data: {}", request);
            return usersRepository.save(request.toEntity());
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("users_email_key")) {
                throw new EmailAlreadyExistsException("Email is already in use.", e);
            } else if (e.getMessage().contains("users_username_key")) {
                throw new UsernameAlreadyExistsException("Username is already taken.", e);
            } else {
                throw e;
            }
        }
    }

    @Transactional
    public User updateUser(UserProfileRequest request) {
        try {
            log.info("Updating into database user with data: {}", request);

            return usersRepository.findById(getUserId())
                    .map(user -> {
                        User updatedUser = request.toEntity();
                        updatedUser.setId(user.getId());
                        updatedUser.setEmail(user.getEmail());
                        preferencesRepository.deleteAllByUser(user);
                        return usersRepository.save(updatedUser);
                    })
                    .orElseThrow(() -> new UserNotExistsException("User not found with userId: " + getUserId(), null));
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("users_username_key")) {
                throw new UsernameAlreadyExistsException("Username is already taken.", e);
            } else {
                throw e;
            }
        }
    }
}
