package com.whatstheplan.users.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatstheplan.users.model.entities.User;
import com.whatstheplan.users.model.response.BasicUserResponse;
import com.whatstheplan.users.model.response.ErrorResponse;
import com.whatstheplan.users.repository.PreferencesRepository;
import com.whatstheplan.users.repository.UsersRepository;
import com.whatstheplan.users.testconfig.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BasicUsersInfoRetrievalControllerIntegrationTest extends BaseIntegrationTest {

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
        User user = User.builder()
                .id(userId)
                .email(email)
                .username("username")
                .firstName("firstName")
                .lastName("lastName")
                .city("city")
                .build();

        usersRepository.save(user);

        // when
        MvcResult result = mockMvc.perform(get("/users-info/" + userId)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andReturn();

        // then
        BasicUserResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), BasicUserResponse.class);

        assertThat(response.getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    void whenAUserGetsDataAndDoesNotExists_thenShouldReturnError() throws Exception {
        //given
        UUID userId = UUID.randomUUID();

        // when
        MvcResult result = mockMvc.perform(get("/users-info/" + userId)
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andReturn();

        // then
        ErrorResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertThat(response.getReason()).isEqualTo("User does not exists.");
    }

    @Test
    void whenAUserGetsDataWithMissingToken_thenWillReturnUnauthorized() throws Exception {
        // given - when - then
        mockMvc.perform(get("/users-info/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}