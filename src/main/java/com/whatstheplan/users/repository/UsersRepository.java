package com.whatstheplan.users.repository;

import com.whatstheplan.users.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UsersRepository extends JpaRepository<User, UUID> {
}
