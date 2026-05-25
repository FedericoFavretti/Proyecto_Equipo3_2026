package com.example.demo.passwordreset;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.passwordreset.dto.ForgotPasswordRequest;
import com.example.demo.passwordreset.dto.PasswordResetResponse;
import com.example.demo.passwordreset.dto.ResetPasswordRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/password")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot")
    public ResponseEntity<PasswordResetResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
        return ResponseEntity.accepted()
                .body(new PasswordResetResponse("Si el correo existe, se enviaron instrucciones de recuperación."));
    }

    @PostMapping("/reset")
    public ResponseEntity<PasswordResetResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(new PasswordResetResponse("La contraseña se actualizó correctamente."));
    }
}
