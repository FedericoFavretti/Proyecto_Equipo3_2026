package com.example.demo.email;

import java.time.Duration;

public interface EmailSender {

    void sendPasswordResetEmail(String to, String resetLink, Duration validFor);
}
