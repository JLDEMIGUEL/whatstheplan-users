package com.whatstheplan.users.repository;

import com.whatstheplan.users.model.entities.Preferences;
import com.whatstheplan.users.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PreferencesRepository extends JpaRepository<Preferences, UUID> {
    void deleteAllByUser(User user);
}
