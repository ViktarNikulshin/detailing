package com.nikulshin.detailing.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "google.calendar")
@Data
public class GoogleCalendarProperties {

    private String calendarId = "primary";
    private String webhookBaseUrl;
    private String webhookPath = "/api/google-calendar/webhook";

    private OAuth oauth = new OAuth();
    private Sync sync = new Sync();
    private Llm llm = new Llm();

    @Data
    public static class OAuth {
        private String clientId;
        private String clientSecret;
        private String refreshToken;
        private String redirectUri = "http://localhost:8080/api/google-calendar/oauth/callback";
    }

    @Data
    public static class Sync {
        private Duration initialLookback = Duration.ofDays(30);
        private Duration lookahead = Duration.ofDays(60);
        private Duration pollInterval = Duration.ofHours(1);
        private int maxResults = 2500;
        private boolean enabled = true;
    }

    @Data
    public static class Llm {
        private String provider = "groq";
        private String apiKey;
        private String baseUrl = "https://api.groq.com/openai/v1";
        private String model = "llama-3.3-70b-versatile";
        private int timeoutSeconds = 30;
        private boolean enabled = true;
    }
}