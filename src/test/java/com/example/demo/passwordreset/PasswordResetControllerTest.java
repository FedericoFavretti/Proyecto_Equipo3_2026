package com.example.demo.passwordreset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.demo.passwordreset.dto.ForgotPasswordRequest;
import com.example.demo.passwordreset.dto.PasswordResetResponse;
import com.example.demo.passwordreset.dto.ResetPasswordRequest;

class PasswordResetControllerTest {

    @Test
    void shouldAcceptForgotPasswordRequestWithGenericResponse() {
        PasswordResetService service = mock(PasswordResetService.class);
        PasswordResetController controller = new PasswordResetController(service);

        ResponseEntity<PasswordResetResponse> response = controller.forgotPassword(
                new ForgotPasswordRequest("user@example.com"));

        verify(service).requestReset("user@example.com");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("Si el correo existe");
    }

    @Test
    void shouldResetPasswordWithOkResponse() {
        PasswordResetService service = mock(PasswordResetService.class);
        PasswordResetController controller = new PasswordResetController(service);

        ResponseEntity<PasswordResetResponse> response = controller.resetPassword(
                new ResetPasswordRequest("raw-token", "NewPassword123"));

        verify(service).resetPassword("raw-token", "NewPassword123");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("contraseña");
    }
}
