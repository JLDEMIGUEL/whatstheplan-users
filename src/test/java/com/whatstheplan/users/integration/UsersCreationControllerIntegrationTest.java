package com.whatstheplan.users.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatstheplan.users.model.ActivityType;
import com.whatstheplan.users.model.email.WelcomeEmail;
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
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static com.whatstheplan.users.model.ActivityType.BASEBALL;
import static com.whatstheplan.users.model.ActivityType.FOOD;
import static com.whatstheplan.users.model.ActivityType.SOCCER;
import static com.whatstheplan.users.testconfig.rabbit.RabbitUtils.poll;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;


@Import(TestChannelBinderConfiguration.class)
class UsersCreationControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PreferencesRepository preferencesRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private OutputDestination output;

    @BeforeEach
    void setUp() {
        usersRepository.deleteAll();
        preferencesRepository.deleteAll();
    }

    @Test
    void whenANewUserCreationRequest_thenShouldStoreUserAndPreferencesAndSendEmailRequest() throws Exception {
        //given
        String email = "test@test.com";
        List<String> preferences = List.of(SOCCER.getName(), BASEBALL.getName(), FOOD.getName());
        UserProfileRequest newUser = UserProfileRequest.builder()
                .username("new_user")
                .firstName("new")
                .lastName("savedUser")
                .city("city")
                .preferences(preferences)
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
                .andExpect(status().isCreated())
                .andReturn();

        // then
        User savedUser = usersRepository.findAll().get(0);
        List<Preferences> savedPreferences = preferencesRepository.findAll();
        UserResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse.class);

        assertThat(response.getUsername()).isEqualTo(newUser.getUsername());
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getFirstName()).isEqualTo(newUser.getFirstName());
        assertThat(response.getLastName()).isEqualTo(newUser.getLastName());
        assertThat(response.getCity()).isEqualTo(newUser.getCity());
        assertThat(response.getPreferences()).containsAll(preferences);

        assertThat(savedUser.getUsername()).isEqualTo(newUser.getUsername());
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getFirstName()).isEqualTo(newUser.getFirstName());
        assertThat(savedUser.getLastName()).isEqualTo(newUser.getLastName());
        assertThat(savedUser.getCity()).isEqualTo(newUser.getCity());
        assertThat(savedUser.getCreatedDate()).isNotNull();
        assertThat(savedUser.getLastModifiedDate()).isNotNull();

        assertThat(savedPreferences.stream().map(Preferences::getActivityType).map(ActivityType::getName).toList())
                .containsAll(preferences);

        WelcomeEmail welcomeEmail = poll(output, "mail", WelcomeEmail.class);
        assertThat(welcomeEmail).isNotNull();
        assertThat(welcomeEmail.getEmail()).isEqualTo(email);
        assertThat(welcomeEmail.getUsername()).isEqualTo(newUser.getUsername());
    }

    @Test
    void whenAUserCreationRequestWithAlreadyExistingUsername_thenWillReturnBadRequest() throws Exception {
        //given
        String email = "test@test.com";
        List<String> preferences = List.of(SOCCER.getName(), BASEBALL.getName(), FOOD.getName());
        UserProfileRequest newUser = UserProfileRequest.builder()
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

        assertThat(usersRepository.count()).isEqualTo(1);
        assertThat(preferencesRepository.count()).isEqualTo(0);
    }

    @Test
    void whenAUserCreationRequestWithAlreadyExistingEmail_thenWillReturnBadRequest() throws Exception {
        //given
        String email = "test@test.com";
        List<String> preferences = List.of(SOCCER.getName(), BASEBALL.getName(), FOOD.getName());
        UserProfileRequest newUser = UserProfileRequest.builder()
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

        assertThat(usersRepository.count()).isEqualTo(1);
        assertThat(preferencesRepository.count()).isEqualTo(0);
    }

    @Test
    void whenAUserCreationRequestWithBadParameters_thenWillReturnBadRequest() throws Exception {
        //given
        String email = "test@test.com";
        UserProfileRequest newUser = UserProfileRequest.builder()
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
        assertThat(response.getReason()).contains("Username is mandatory.", "First name is mandatory.",
                "Last name is mandatory.", "City name is mandatory.");

        assertThat(usersRepository.count()).isEqualTo(0);
        assertThat(preferencesRepository.count()).isEqualTo(0);
    }

    @Test
    void whenAUserCreationRequestWithTokenMissingEmail_thenWillReturnBadRequest() throws Exception {
        //given
        List<String> preferences = List.of(SOCCER.getName(), BASEBALL.getName(), FOOD.getName());
        UserProfileRequest newUser = UserProfileRequest.builder()
                .username("new_user")
                .firstName("new")
                .lastName("savedUser")
                .city("city")
                .preferences(preferences)
                .build();


        // when
        MvcResult result = mockMvc.perform(post("/users")
                        .with(jwt()
                                .jwt(jwt -> jwt
                                        .claim("sub", UUID.randomUUID()))
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
        UserProfileRequest newUser = UserProfileRequest.builder()
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
                .andExpect(status().isUnauthorized());

        // then
        assertThat(usersRepository.count()).isEqualTo(0);
        assertThat(preferencesRepository.count()).isEqualTo(0);
    }
}