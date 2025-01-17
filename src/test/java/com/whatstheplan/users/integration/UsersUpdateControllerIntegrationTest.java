package com.whatstheplan.users.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatstheplan.users.model.ActivityType;
import com.whatstheplan.users.model.entities.Preferences;
import com.whatstheplan.users.model.entities.User;
import com.whatstheplan.users.model.request.UserProfileRequest;
import com.whatstheplan.users.model.response.ErrorResponse;
import com.whatstheplan.users.model.response.UserResponse;
import com.whatstheplan.users.repository.PreferencesRepository;
import com.whatstheplan.users.repository.UsersRepository;
import com.whatstheplan.users.testconfig.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static com.whatstheplan.users.model.ActivityType.BASEBALL;
import static com.whatstheplan.users.model.ActivityType.COOKING;
import static com.whatstheplan.users.model.ActivityType.FOOD;
import static com.whatstheplan.users.model.ActivityType.LANGUAGE_LEARNING;
import static com.whatstheplan.users.model.ActivityType.SOCCER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

class UsersUpdateControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PreferencesRepository preferencesRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        usersRepository.deleteAll();
        preferencesRepository.deleteAll();
    }

    @Test
    void whenAnUserUpdateRequest_thenShouldUpdateUserAndPreferences() throws Exception {
        //given
        UUID userId = UUID.randomUUID();
        String email = "test@test.com";
        List<String> preferences = List.of(SOCCER.getName(), BASEBALL.getName(), FOOD.getName());
        User user = User.builder()
                .id(userId)
                .email(email)
                .username("username")
                .firstName("firstName")
                .lastName("lastName")
                .city("city")
                .build();

        List<Preferences> preferencesEntities = preferences.stream()
                .map(ActivityType::from)
                .map(at -> new Preferences(null, at, user))
                .toList();

        user.setPreferences(preferencesEntities);

        usersRepository.save(user);

        List<String> newPreferences = List.of(LANGUAGE_LEARNING.getName(), COOKING.getName());
        UserProfileRequest updateUser = UserProfileRequest.builder()
                .username("new_username")
                .firstName("new_firstName")
                .lastName("new_lastName")
                .city("new_city")
                .preferences(newPreferences)
                .build();

        // when
        MvcResult result = mockMvc.perform(put("/users")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .claim("sub", userId)
                                        .claim("email", email))
                                .authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsBytes(updateUser)))
                .andExpect(status().isOk())
                .andReturn();

        // then
        User savedUser = usersRepository.findAll().get(0);
        List<Preferences> savedPreferences = preferencesRepository.findAll();
        UserResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse.class);

        assertThat(response.getUsername()).isEqualTo(updateUser.getUsername());
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getFirstName()).isEqualTo(updateUser.getFirstName());
        assertThat(response.getLastName()).isEqualTo(updateUser.getLastName());
        assertThat(response.getCity()).isEqualTo(updateUser.getCity());
        assertThat(response.getPreferences()).containsAll(newPreferences);

        assertThat(savedUser.getUsername()).isEqualTo(updateUser.getUsername());
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getFirstName()).isEqualTo(updateUser.getFirstName());
        assertThat(savedUser.getLastName()).isEqualTo(updateUser.getLastName());
        assertThat(savedUser.getCity()).isEqualTo(updateUser.getCity());
        assertThat(savedUser.getCreatedDate()).isNotNull();
        assertThat(savedUser.getLastModifiedDate()).isNotNull();

        assertThat(savedPreferences.stream().map(Preferences::getActivityType).map(ActivityType::getName).toList())
                .containsAll(newPreferences);
    }


    @Test
    void whenAnUserUpdateRequestWithNonExistingUser_thenWillReturnBadRequest() throws Exception {
        //given
        UUID userId = UUID.randomUUID();
        String email = "test@test.com";

        List<String> newPreferences = List.of(LANGUAGE_LEARNING.getName(), COOKING.getName());
        UserProfileRequest updateUser = UserProfileRequest.builder()
                .username("new_username")
                .firstName("new_firstName")
                .lastName("new_lastName")
                .city("new_city")
                .preferences(newPreferences)
                .build();

        // when
        MvcResult result = mockMvc.perform(put("/users")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .claim("sub", userId)
                                        .claim("email", email))
                                .authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsBytes(updateUser)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // then
        ErrorResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertThat(response.getReason()).isEqualTo("User does not exists.");

        assertThat(usersRepository.count()).isEqualTo(0);
        assertThat(preferencesRepository.count()).isEqualTo(0);
    }

    @Test
    void whenAnUserUpdateRequestToAlreadyExistingUsername_thenWillReturnBadRequest() throws Exception {
        //given
        UUID userId = UUID.randomUUID();
        String email = "test@test.com";
        List<String> preferences = List.of(SOCCER.getName(), BASEBALL.getName(), FOOD.getName());
        User user = User.builder()
                .id(userId)
                .email(email)
                .username("username")
                .firstName("firstName")
                .lastName("lastName")
                .city("city")
                .build();

        List<Preferences> preferencesEntities = preferences.stream()
                .map(ActivityType::from)
                .map(at -> new Preferences(null, at, user))
                .toList();

        user.setPreferences(preferencesEntities);

        usersRepository.save(user);

        User otherUser = User.builder()
                .id(UUID.randomUUID())
                .email("other@test.com")
                .username("otherUser")
                .firstName("firstName")
                .lastName("lastName")
                .city("city")
                .build();
        usersRepository.save(otherUser);

        List<String> newPreferences = List.of(LANGUAGE_LEARNING.getName(), COOKING.getName());
        UserProfileRequest updateUser = UserProfileRequest.builder()
                .username("otherUser")
                .firstName("otherFirstName")
                .lastName("otherLastName")
                .city("otherCity")
                .preferences(newPreferences)
                .build();

        // when
        MvcResult result = mockMvc.perform(put("/users")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .claim("sub", userId)
                                        .claim("email", email))
                                .authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsBytes(updateUser)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // then
        ErrorResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertThat(response.getReason()).contains("Username already exists.");

        User savedUser = usersRepository.findAll().get(0);
        List<Preferences> savedPreferences = preferencesRepository.findAll();

        assertThat(savedUser.getUsername()).isEqualTo(user.getUsername());
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(savedUser.getLastName()).isEqualTo(user.getLastName());
        assertThat(savedUser.getCity()).isEqualTo(user.getCity());
        assertThat(savedUser.getCreatedDate()).isNotNull();

        assertThat(savedPreferences.stream().map(Preferences::getActivityType).map(ActivityType::getName).toList())
                .containsAll(preferences);
    }

    @Test
    void whenAnUserUpdateRequestWithBadParameters_thenWillReturnBadRequest() throws Exception {
        //given
        UUID userId = UUID.randomUUID();
        String email = "test@test.com";
        List<String> preferences = List.of(SOCCER.getName(), BASEBALL.getName(), FOOD.getName());
        User user = User.builder()
                .id(userId)
                .email(email)
                .username("username")
                .firstName("firstName")
                .lastName("lastName")
                .city("city")
                .build();

        List<Preferences> preferencesEntities = preferences.stream()
                .map(ActivityType::from)
                .map(at -> new Preferences(null, at, user))
                .toList();

        user.setPreferences(preferencesEntities);

        usersRepository.save(user);

        List<String> newPreferences = List.of(LANGUAGE_LEARNING.getName(), COOKING.getName());
        UserProfileRequest updateUser = UserProfileRequest.builder()
                .username("")
                .firstName("")
                .lastName("")
                .city("")
                .preferences(newPreferences)
                .build();

        // when
        MvcResult result = mockMvc.perform(put("/users")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .claim("sub", userId)
                                        .claim("email", email))
                                .authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsBytes(updateUser)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // then
        ErrorResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertThat(response.getReason()).contains("Username is mandatory.", "First name is mandatory.",
                "Last name is mandatory.", "City name is mandatory.");

        User savedUser = usersRepository.findAll().get(0);
        List<Preferences> savedPreferences = preferencesRepository.findAll();

        assertThat(savedUser.getUsername()).isEqualTo(user.getUsername());
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(savedUser.getLastName()).isEqualTo(user.getLastName());
        assertThat(savedUser.getCity()).isEqualTo(user.getCity());
        assertThat(savedUser.getCreatedDate()).isNotNull();

        assertThat(savedPreferences.stream().map(Preferences::getActivityType).map(ActivityType::getName).toList())
                .containsAll(preferences);
    }

    @Test
    void whenAnUserUpdateRequestWithTokenMissingEmail_thenWillReturnBadRequest() throws Exception {
        //given
        UUID userId = UUID.randomUUID();
        String email = "test@test.com";
        List<String> preferences = List.of(SOCCER.getName(), BASEBALL.getName(), FOOD.getName());
        User user = User.builder()
                .id(userId)
                .email(email)
                .username("username")
                .firstName("firstName")
                .lastName("lastName")
                .city("city")
                .build();

        List<Preferences> preferencesEntities = preferences.stream()
                .map(ActivityType::from)
                .map(at -> new Preferences(null, at, user))
                .toList();

        user.setPreferences(preferencesEntities);

        usersRepository.save(user);

        List<String> newPreferences = List.of(LANGUAGE_LEARNING.getName(), COOKING.getName());
        UserProfileRequest updateUser = UserProfileRequest.builder()
                .username("new_username")
                .firstName("new_firstName")
                .lastName("new_lastName")
                .city("new_city")
                .preferences(newPreferences)
                .build();

        // when
        MvcResult result = mockMvc.perform(put("/users")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .claim("sub", userId))
                                .authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsBytes(updateUser)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // then
        ErrorResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertThat(response.getReason()).isEqualTo("Invalid token, email not found.");

        User savedUser = usersRepository.findAll().get(0);
        List<Preferences> savedPreferences = preferencesRepository.findAll();

        assertThat(savedUser.getUsername()).isEqualTo(user.getUsername());
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(savedUser.getLastName()).isEqualTo(user.getLastName());
        assertThat(savedUser.getCity()).isEqualTo(user.getCity());
        assertThat(savedUser.getCreatedDate()).isNotNull();

        assertThat(savedPreferences.stream().map(Preferences::getActivityType).map(ActivityType::getName).toList())
                .containsAll(preferences);
    }

    @Test
    void whenAnUserUpdateRequestWithMissingToken_thenWillReturnUnauthorized() throws Exception {
        //given
        UUID userId = UUID.randomUUID();
        String email = "test@test.com";
        List<String> preferences = List.of(SOCCER.getName(), BASEBALL.getName(), FOOD.getName());
        User user = User.builder()
                .id(userId)
                .email(email)
                .username("username")
                .firstName("firstName")
                .lastName("lastName")
                .city("city")
                .build();

        List<Preferences> preferencesEntities = preferences.stream()
                .map(ActivityType::from)
                .map(at -> new Preferences(null, at, user))
                .toList();

        user.setPreferences(preferencesEntities);

        usersRepository.save(user);

        List<String> newPreferences = List.of(LANGUAGE_LEARNING.getName(), COOKING.getName());
        UserProfileRequest updateUser = UserProfileRequest.builder()
                .username("new_username")
                .firstName("new_firstName")
                .lastName("new_lastName")
                .city("new_city")
                .preferences(newPreferences)
                .build();

        // when
        mockMvc.perform(put("/users")
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsBytes(updateUser)))
                .andExpect(status().isForbidden());

        // then
        User savedUser = usersRepository.findAll().get(0);
        List<Preferences> savedPreferences = preferencesRepository.findAll();

        assertThat(savedUser.getUsername()).isEqualTo(user.getUsername());
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(savedUser.getLastName()).isEqualTo(user.getLastName());
        assertThat(savedUser.getCity()).isEqualTo(user.getCity());
        assertThat(savedUser.getCreatedDate()).isNotNull();

        assertThat(savedPreferences.stream().map(Preferences::getActivityType).map(ActivityType::getName).toList())
                .containsAll(preferences);
    }
}