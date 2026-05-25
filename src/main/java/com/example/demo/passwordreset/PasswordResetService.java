package com.example.demo.passwordreset;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.email.EmailSender;
import com.example.demo.user.domain.User;
import com.example.demo.user.repository.UserRepository;

@Service
public class PasswordResetService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetProperties passwordResetProperties;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            EmailSender emailSender,
            PasswordEncoder passwordEncoder,
            PasswordResetProperties passwordResetProperties) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailSender = emailSender;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetProperties = passwordResetProperties;
    }

    @Transactional
    public void requestReset(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();
        Instant now = Instant.now();
        invalidateActiveTokens(user, now);

        String rawToken = generateSecureToken();
        PasswordResetToken passwordResetToken = new PasswordResetToken(
                user,
                hashToken(rawToken),
                now.plus(passwordResetProperties.ttl()));

        passwordResetTokenRepository.save(passwordResetToken);
        emailSender.sendPasswordResetEmail(user.getEmail(), buildResetLink(rawToken), passwordResetProperties.ttl());
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByTokenHash(hashToken(rawToken))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid password reset token"));

        Instant now = Instant.now();
        if (passwordResetToken.isUsed() || passwordResetToken.isExpired(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expired or already used password reset token");
        }

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        passwordResetToken.setUsedAt(now);

        userRepository.save(user);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    private void invalidateActiveTokens(User user, Instant now) {
        List<PasswordResetToken> activeTokens = passwordResetTokenRepository.findAllByUserAndUsedAtIsNull(user);
        if (activeTokens.isEmpty()) {
            return;
        }

        activeTokens.forEach(token -> token.setUsedAt(now));
        passwordResetTokenRepository.saveAll(activeTokens);
    }

    private String buildResetLink(String rawToken) {
        String encodedToken = URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        return "%s?token=%s".formatted(passwordResetProperties.frontendUrl(), encodedToken);
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    static String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }
}
