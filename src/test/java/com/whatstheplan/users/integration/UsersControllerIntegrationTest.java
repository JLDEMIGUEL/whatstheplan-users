package com.whatstheplan.users.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatstheplan.users.model.ActivityType;
import com.whatstheplan.users.model.entities.Preferences;
import com.whatstheplan.users.model.entities.User;
import com.whatstheplan.users.model.request.UserCreationRequest;
import com.whatstheplan.users.model.response.ErrorResponse;
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

import static com.whatstheplan.users.model.ActivityType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

class UsersControllerIntegrationTest extends BaseIntegrationTest {

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
    void whenANewUserCreationRequest_thenShouldStoreUserAndPreferences() throws Exception {
        //given
        String email = "test@test.com";
        List<String> preferences = List.of(SOCCER.getName(), BASEBALL.getName(), FOOD.getName());
        UserCreationRequest newUser = UserCreationRequest.builder()
                .username("new_user")
                .firstName("new")
                .lastName("savedUser")
                .city("city")
                .preferences(preferences)
                .build();

        // when
        mockMvc.perform(post("/users")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .claim("sub", UUID.randomUUID())
                                        .claim("email", email))
                                .authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsBytes(newUser)))
                .andExpect(status().isCreated());

        // then
        User savedUser = usersRepository.findAll().get(0);
        List<Preferences> savedPreferences = preferencesRepository.findAll();

        assertThat(savedUser.getUsername()).isEqualTo(newUser.getUsername());
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getFirstName()).isEqualTo(newUser.getFirstName());
        assertThat(savedUser.getLastName()).isEqualTo(newUser.getLastName());
        assertThat(savedUser.getCity()).isEqualTo(newUser.getCity());
        assertThat(savedUser.getCreatedDate()).isNotNull();
        assertThat(savedUser.getLastModifiedDate()).isNotNull();

        assertThat(savedPreferences.stream().map(Preferences::getActivityType).map(ActivityType::getName).toList())
                .containsAll(preferences);
    }


    @Test
    void whenAUserCreationRequestWithAlreadyExistingUsername_thenWillReturnBadRequest() throws Exception {
        //given
        String email = "test@test.com";
        List<String> preferences = List.of(SOCCER.getName(), BASEBALL.getName(), FOOD.getName());
        UserCreationRequest newUser = UserCreationRequest.builder()
                .username("new_user")
                .firstName("new")
                .lastName("savedUser")
                .city("city")
                .preferences(preferences)
                .build();

        usersRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email("otheremail@mail.com")
                .username("new_user")
                .firstName("new")
                .lastName("savedUser")
                .city("city")
                .build());

        // when
        MvcResult result = mockMvc.perform(post("/users")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .claim("sub", UUID.randomUUID())
                                        .claim("email", email))
                                .authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsBytes(newUser)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // then
        ErrorResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertThat(response.getReason()).isEqualTo("Username already exists.");

        assertThat(usersRepository.count()).isEqualTo(0);
        assertThat(preferencesRepository.count()).isEqualTo(0);
    }

    @Test
    void whenAUserCreationRequestWithAlreadyExistingEmail_thenWillReturnBadRequest() throws Exception {
        //given
        String email = "test@test.com";
        List<String> preferences = List.of(SOCCER.getName(), BASEBALL.getName(), FOOD.getName());
        UserCreationRequest newUser = UserCreationRequest.builder()
                .username("new_user")
                .firstName("new")
                .lastName("savedUser")
                .city("city")
                .preferences(preferences)
                .build();

        usersRepository.save(User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .username("other_user")
                .firstName("new")
                .lastName("savedUser")
                .city("city")
                .build());

        // when
        MvcResult result = mockMvc.perform(post("/users")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .claim("sub", UUID.randomUUID())
                                        .claim("email", email))
                                .authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsBytes(newUser)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // then
        ErrorResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertThat(response.getReason()).isEqualTo("Email already exists.");

        assertThat(usersRepository.count()).isEqualTo(0);
        assertThat(preferencesRepository.count()).isEqualTo(0);
    }

    @Test
    void whenAUserCreationRequestWithBadParameters_thenWillReturnBadRequest() throws Exception {
        //given
        String email = "test@test.com";
        UserCreationRequest newUser = UserCreationRequest.builder()
                .username("")
                .build();


        // when
        MvcResult result = mockMvc.perform(post("/users")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .claim("sub", UUID.randomUUID())
                                        .claim("email", email))
                                .authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsBytes(newUser)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // then
        ErrorResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertThat(response.getReason()).isEqualTo("Username is mandatory.");

        assertThat(usersRepository.count()).isEqualTo(0);
        assertThat(preferencesRepository.count()).isEqualTo(0);
    }

    @Test
    void whenAUserCreationRequestWithTokenMissingEmail_thenWillReturnBadRequest() throws Exception {
        //given
        List<String> preferences = List.of(SOCCER.getName(), BASEBALL.getName(), FOOD.getName());
        UserCreationRequest newUser = UserCreationRequest.builder()
                .username("new_user")
                .firstName("new")
                .lastName("savedUser")
                .city("city")
                .preferences(preferences)
                .build();


        // when
        MvcResult result = mockMvc.perform(post("/users")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_user")))
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsBytes(newUser)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // then
        ErrorResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertThat(response.getReason()).isEqualTo("Invalid token, email not found.");

        assertThat(usersRepository.count()).isEqualTo(0);
        assertThat(preferencesRepository.count()).isEqualTo(0);
    }

    @Test
    void whenAUserCreationRequestWithMissingToken_thenWillReturnUnauthorized() throws Exception {
        //given
        List<String> preferences = List.of(SOCCER.getName(), BASEBALL.getName(), FOOD.getName());
        UserCreationRequest newUser = UserCreationRequest.builder()
                .username("new_user")
                .firstName("new")
                .lastName("savedUser")
                .city("city")
                .preferences(preferences)
                .build();


        // when
        mockMvc.perform(post("/users")
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsBytes(newUser)))
                .andExpect(status().isForbidden());

        // then
        assertThat(usersRepository.count()).isEqualTo(0);
        assertThat(preferencesRepository.count()).isEqualTo(0);
    }
}