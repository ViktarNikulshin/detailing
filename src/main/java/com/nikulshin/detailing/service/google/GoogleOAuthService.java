package com.nikulshin.detailing.service.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.nikulshin.detailing.configuration.GoogleCalendarProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuthService {

    private final GoogleCalendarProperties properties;

    private static final List<String> SCOPES = Collections.singletonList(
            "https://www.googleapis.com/auth/calendar"
    );

    private static final String USER_ID = "user";

    private GoogleAuthorizationCodeFlow flow;
    private Credential storedCredential;

    private synchronized GoogleAuthorizationCodeFlow getFlow() throws IOException, GeneralSecurityException {
        if (flow == null) {
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                    GsonFactory.getDefaultInstance(),
                    new StringReader(String.format("""
                            {
                              "web": {
                                "client_id": "%s",
                                "client_secret": "%s",
                                "redirect_uris": ["%s"],
                                "auth_uri": "https://accounts.google.com/o/oauth2/auth",
                                "token_uri": "https://oauth2.googleapis.com/token"
                              }
                            }
                            """, properties.getOauth().getClientId(), properties.getOauth().getClientSecret(), properties.getOauth().getRedirectUri()))
            );

            DataStoreFactory dataStoreFactory = new MemoryDataStoreFactory();

            flow = new GoogleAuthorizationCodeFlow.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    clientSecrets,
                    SCOPES)
                    .setDataStoreFactory(dataStoreFactory)
                    .setAccessType("offline")
                    .build();
        }
        return flow;
    }

    public String getAuthorizationUrl() throws IOException, GeneralSecurityException {
        return getFlow().newAuthorizationUrl()
                .setRedirectUri(properties.getOauth().getRedirectUri())
                .setAccessType("offline")
                .set("prompt", "consent")
                .build();
    }

    public void handleCallback(String code) throws IOException, GeneralSecurityException {
        GoogleTokenResponse tokenResponse = getFlow().newTokenRequest(code)
                .setRedirectUri(properties.getOauth().getRedirectUri())
                .execute();

        storedCredential = getFlow().createAndStoreCredential(tokenResponse, USER_ID);
        log.info("OAuth tokens received. Refresh token: {}",
                storedCredential.getRefreshToken() != null ? storedCredential.getRefreshToken() : "null");
    }

    public Credential getCredential() throws IOException, GeneralSecurityException {
        // 1. Если есть сохранённый в памяти (после callback)
        if (storedCredential != null) {
            if (storedCredential.getExpiresInSeconds() != null && storedCredential.getExpiresInSeconds() <= 60) {
                log.info("Access token expired, refreshing...");
                storedCredential.refreshToken();
            }
            return storedCredential;
        }

        // 2. Если есть refresh_token в конфиге — создаём credential и рефрешим
        if (properties.getOauth().getRefreshToken() != null && !properties.getOauth().getRefreshToken().isBlank()) {
            log.info("Creating credential from config refresh token");
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                    .setJsonFactory(GsonFactory.getDefaultInstance())
                    .setClientSecrets(properties.getOauth().getClientId(), properties.getOauth().getClientSecret())
                    .build()
                    .setRefreshToken(properties.getOauth().getRefreshToken());

            credential.refreshToken();
            storedCredential = credential;
            return credential;
        }

        throw new IllegalStateException("No OAuth credentials available. Visit /api/google-calendar/auth-url to authorize.");
    }

    public boolean isAuthorized() {
        try {
            Credential c = getCredential();
            return c.getAccessToken() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public void clearStoredCredential() {
        storedCredential = null;
    }
}