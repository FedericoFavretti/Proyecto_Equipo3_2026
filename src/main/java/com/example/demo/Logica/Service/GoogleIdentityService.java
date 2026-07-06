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

@Service
public class GoogleIdentityService {
    private static final String MENSAJE_TOKEN_INVALIDO = "No fue posible completar la autenticación con Google. Por favor, intente nuevamente o regístrese con correo y contraseña.";
    private static final String MENSAJE_ERROR_CONEXION = "No se pudo conectar con Google en este momento. Por favor, verifique su conexión e inténtelo nuevamente.";
    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    public DtGoogleUserInfo obtenerDatosUsuario(String idToken) {
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(GOOGLE_USERINFO_URL))
                    .header("Authorization", "Bearer " + idToken)
                    .GET()
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (httpResponse.statusCode() != 200) {
                throw new BusinessRuleException(MENSAJE_TOKEN_INVALIDO);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(httpResponse.body());
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
