package com.whatstheplan.users.services;

import com.whatstheplan.users.exceptions.EmailAlreadyExistsException;
import com.whatstheplan.users.exceptions.UsernameAlreadyExistsException;
import com.whatstheplan.users.model.entities.User;
import com.whatstheplan.users.model.request.UserCreationRequest;
import com.whatstheplan.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;

    public User saveUser(UserCreationRequest request) {
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
}
