package com.nikulshin.detailing.controller;

import com.nikulshin.detailing.service.google.GoogleOAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/api/google-calendar")
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarAuthController {

    private final GoogleOAuthService oauthService;

    @GetMapping("/auth-url")
    public ResponseEntity<AuthUrlResponse> getAuthUrl() {
        try {
            String url = oauthService.getAuthorizationUrl();
            return ResponseEntity.ok(new AuthUrlResponse(url));
        } catch (IOException | GeneralSecurityException e) {
            log.error("Failed to generate auth URL: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/oauth/callback")
    public ResponseEntity<String> handleCallback(@RequestParam String code,
                                                  @RequestParam(required = false) String error) {
        if (error != null) {
            log.error("OAuth error: {}", error);
            return ResponseEntity.badRequest().body("Authorization failed: " + error);
        }

        try {
            oauthService.handleCallback(code);
            return ResponseEntity.ok("""
                    <html>
                    <body style="font-family: sans-serif; text-align: center; padding: 50px;">
                        <h2>✅ Авторизация успешна!</h2>
                        <p>Токены получены и сохранены.</p>
                        <p>Теперь можно закрыть это окно и использовать синхронизацию календаря.</p>
                        <script>setTimeout(() => window.close(), 3000);</script>
                    </body>
                    </html>
                    """);
        } catch (IOException | GeneralSecurityException e) {
            log.error("OAuth callback failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Authorization failed: " + e.getMessage());
        }
    }

    @GetMapping("/auth-status")
    public ResponseEntity<AuthStatusResponse> getAuthStatus() {
        return ResponseEntity.ok(new AuthStatusResponse(oauthService.isAuthorized()));
    }

    @lombok.Data
    public static class AuthUrlResponse {
        private final String authUrl;
    }

    @lombok.Data
    public static class AuthStatusResponse {
        private final boolean authorized;
    }
}