package com.example.demo.Logica.Service;

import com.example.demo.Logica.DataTypes.shared.DtGoogleUserInfo;
import com.example.demo.Logica.Exceptions.BusinessRuleException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Valida ID tokens JWT de Google generados por el flujo móvil
 * (google_sign_in v7 con Credential Manager).
 * Usa el endpoint tokeninfo de Google, que acepta JWT y devuelve
 * los datos del usuario sin necesidad de un OAuth access token.
 */
@Service
public class GoogleMobileIdentityService {

    private static final String MENSAJE_TOKEN_INVALIDO =
            "No fue posible completar la autenticación con Google. Por favor, intente nuevamente o regístrese con correo y contraseña.";
    private static final String MENSAJE_ERROR_CONEXION =
            "No se pudo conectar con Google en este momento. Por favor, verifique su conexión e inténtelo nuevamente.";
    private static final String GOOGLE_TOKENINFO_URL =
            "https://oauth2.googleapis.com/tokeninfo?id_token=";

    public DtGoogleUserInfo obtenerDatosUsuario(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new BusinessRuleException(MENSAJE_TOKEN_INVALIDO);
        }
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(GOOGLE_TOKENINFO_URL + idToken.trim()))
                    .GET()
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(
                    httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != 200) {
                throw new BusinessRuleException(MENSAJE_TOKEN_INVALIDO);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(httpResponse.body());

            if (!json.has("email")) {
                throw new BusinessRuleException(MENSAJE_TOKEN_INVALIDO);
            }

            return DtGoogleUserInfo.builder()
                    .email(json.get("email").asText())
                    .nombre(json.has("given_name") ? json.get("given_name").asText() : "Usuario")
                    .apellido(json.has("family_name") ? json.get("family_name").asText() : "")
                    .foto(json.has("picture") ? json.get("picture").asText() : null)
                    .build();

        } catch (BusinessRuleException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessRuleException(MENSAJE_ERROR_CONEXION);
        }
    }
}
