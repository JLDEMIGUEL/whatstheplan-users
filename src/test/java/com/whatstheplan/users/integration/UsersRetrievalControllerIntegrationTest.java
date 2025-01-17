package com.whatstheplan.users.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatstheplan.users.model.ActivityType;
import com.whatstheplan.users.model.entities.Preferences;
import com.whatstheplan.users.model.entities.User;
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
import static com.whatstheplan.users.model.ActivityType.FOOD;
import static com.whatstheplan.users.model.ActivityType.SOCCER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UsersRetrievalControllerIntegrationTest extends BaseIntegrationTest {

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
    void whenAUserGetsData_thenShouldReturnUserAndPreferences() throws Exception {
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

        // when
        MvcResult result = mockMvc.perform(get("/users")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .claim("sub", userId)
                                        .claim("email", email))
                                .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isOk())
                .andReturn();

        // then
        UserResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse.class);

        assertThat(response.getUsername()).isEqualTo(user.getUsername());
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(response.getLastName()).isEqualTo(user.getLastName());
        assertThat(response.getCity()).isEqualTo(user.getCity());
        assertThat(response.getPreferences()).containsAll(preferences);
    }

    @Test
    void whenAUserGetsDataAndDoesNotExists_thenShouldReturnError() throws Exception {
        //given
        UUID userId = UUID.randomUUID();
        String email = "test@test.com";

        // when
        MvcResult result = mockMvc.perform(get("/users")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .claim("sub", userId)
                                        .claim("email", email))
                                .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isBadRequest())
                .andReturn();

        // then
        ErrorResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertThat(response.getReason()).isEqualTo("User does not exists.");
    }

    @Test
    void whenAUserGetsDataWithTokenMissingEmail_thenWillReturnBadRequest() throws Exception {
        // given - when
        MvcResult result = mockMvc.perform(get("/users")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .claim("sub", UUID.randomUUID()))
                                .authorities(new SimpleGrantedAuthority("ROLE_user"))))
                .andExpect(status().isBadRequest())
                .andReturn();

        // then
        ErrorResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertThat(response.getReason()).isEqualTo("Invalid token, email not found.");
    }

    @Test
    void whenAUserGetsDataWithMissingToken_thenWillReturnUnauthorized() throws Exception {
        // given - when - then
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }
}