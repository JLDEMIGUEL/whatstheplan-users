package com.whatstheplan.users.services;

import com.whatstheplan.users.model.email.WelcomeEmail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    public static final String WELCOME_EMAIL_BINDING = "welcomeEmail-out-0";
    private final StreamBridge streamBridge;

    public void sendWelcomeEmail(String username, String email) {
        log.info("Sending welcome email request to RabbitMQ with for user {} and email {}",
                username, email);
        streamBridge.send(WELCOME_EMAIL_BINDING,
                MessageBuilder.withPayload(WelcomeEmail.builder()
                        .email(email)
                        .username(username)
                        .build()
                ).build()
        );
    }
}
