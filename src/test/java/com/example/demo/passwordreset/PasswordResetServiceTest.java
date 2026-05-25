package com.example.demo.passwordreset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.email.EmailSender;
import com.example.demo.user.domain.AccountStatus;
import com.example.demo.user.domain.Role;
import com.example.demo.user.domain.User;
import com.example.demo.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    private static final PasswordResetProperties PROPERTIES = new PasswordResetProperties(
            "http://localhost:4200/reset-password",
            Duration.ofMinutes(30));

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailSender emailSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(
                userRepository,
                passwordResetTokenRepository,
                emailSender,
                passwordEncoder,
                PROPERTIES);
    }

    @Test
    void shouldGenerateHashedTokenAndSendResetEmailWhenUserExists() {
        User user = new User("user@example.com", "encoded-password", Role.CUSTOMER, AccountStatus.ACTIVE, null);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.findAllByUserAndUsedAtIsNull(user)).thenReturn(List.of());

        passwordResetService.requestReset("user@example.com");

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());

        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getUser()).isSameAs(user);
        assertThat(savedToken.getTokenHash()).hasSize(64);
        assertThat(savedToken.getUsedAt()).isNull();
        assertThat(savedToken.getExpiresAt()).isAfter(Instant.now());

        ArgumentCaptor<String> resetLinkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).sendPasswordResetEmail(
                eq("user@example.com"),
                resetLinkCaptor.capture(),
                eq(PROPERTIES.ttl()));

        String resetLink = resetLinkCaptor.getValue();
        assertThat(resetLink).startsWith("http://localhost:4200/reset-password?token=");

        String rawToken = resetLink.substring(resetLink.indexOf("token=") + "token=".length());
        assertThat(rawToken).isNotBlank();
        assertThat(rawToken).isNotEqualTo(savedToken.getTokenHash());
    }

    @Test
    void shouldInvalidatePreviousActiveTokensBeforeSavingNewOne() {
        User user = new User("user@example.com", "encoded-password", Role.CUSTOMER, AccountStatus.ACTIVE, null);
        PasswordResetToken previousToken = new PasswordResetToken(
                user,
                PasswordResetService.hashToken("previous-token"),
                Instant.now().plus(Duration.ofMinutes(5)));

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.findAllByUserAndUsedAtIsNull(user)).thenReturn(List.of(previousToken));

        passwordResetService.requestReset("user@example.com");

        assertThat(previousToken.getUsedAt()).isNotNull();
        verify(passwordResetTokenRepository).saveAll(List.of(previousToken));
    }

    @Test
    void shouldNotRevealWhetherEmailExists() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        passwordResetService.requestReset("missing@example.com");

        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailSender, never()).sendPasswordResetEmail(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void shouldResetPasswordWhenTokenIsValid() {
        User user = new User("user@example.com", "old-password", Role.CUSTOMER, AccountStatus.ACTIVE, null);
        String rawToken = "plain-reset-token";
        PasswordResetToken token = new PasswordResetToken(
                user,
                PasswordResetService.hashToken(rawToken),
                Instant.now().plus(PROPERTIES.ttl()));

        when(passwordResetTokenRepository.findByTokenHash(token.getTokenHash())).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NewPassword123")).thenReturn("encoded-password");

        passwordResetService.resetPassword(rawToken, "NewPassword123");

        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(token.getUsedAt()).isNotNull();
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).save(token);
    }

    @Test
    void shouldRejectExpiredToken() {
        User user = new User("user@example.com", "old-password", Role.CUSTOMER, AccountStatus.ACTIVE, null);
        String rawToken = "expired-token";
        PasswordResetToken token = new PasswordResetToken(
                user,
                PasswordResetService.hashToken(rawToken),
                Instant.now().minusSeconds(30));

        when(passwordResetTokenRepository.findByTokenHash(token.getTokenHash())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> passwordResetService.resetPassword(rawToken, "NewPassword123"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(userRepository, never()).save(any());
    }
}
