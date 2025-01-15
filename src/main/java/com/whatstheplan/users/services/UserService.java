package com.whatstheplan.users.services;

import com.whatstheplan.users.exceptions.EmailAlreadyExistsException;
import com.whatstheplan.users.exceptions.GenericException;
import com.whatstheplan.users.exceptions.UsernameAlreadyExistsException;
import com.whatstheplan.users.model.entities.User;
import com.whatstheplan.users.model.request.UserCreationRequest;
import com.whatstheplan.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;

    @Transactional
    public User saveUser(UserCreationRequest request) {
        try {
            return usersRepository.save(request.toEntity());
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("email")) {
                throw new EmailAlreadyExistsException("Email is already in use.", e);
            } else if (e.getMessage().contains("username")) {
                throw new UsernameAlreadyExistsException("Username is already taken.", e);
            } else {
                throw new GenericException("An error occurred while creating the user.", e);
            }
        }
    }
}
