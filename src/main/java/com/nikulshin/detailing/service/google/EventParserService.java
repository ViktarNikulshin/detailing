package com.nikulshin.detailing.service.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikulshin.detailing.configuration.GoogleCalendarProperties;
import com.nikulshin.detailing.model.domain.CalendarEvent;
import com.nikulshin.detailing.service.DictionaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventParserService {

    private final GoogleCalendarProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient;
    private final DictionaryService dictionaryService;

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?:\\+?7|8)[\\s\\-]?\\(?\\d{3}\\)?[\\s\\-]?\\d{3}[\\s\\-]?\\d{2}[\\s\\-]?\\d{2}"
    );

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "(?:^|\\s)([А-ЯЁ][а-яё]+(?:\\s+[А-ЯЁ][а-яё]+){1,2})(?:\\s|$|,|;|\\.|:|\\||-)",
            Pattern.UNICODE_CHARACTER_CLASS
    );

    public ParsedEvent parse(String description) {
        ParsedEvent parsed = new ParsedEvent();

        String phone = extractPhone(description);
        parsed.setPhone(phone);

        String name = extractName(description, phone);
        parsed.setName(name);



        String comment = extractComment(description, name, phone);
        parsed.setComment(comment);

        parsed.setConfidence(calculateConfidence(parsed));
        return parsed;
    }

    public ParsedEvent parseWithLlm(String description) {
        if (!properties.getLlm().isEnabled() || properties.getLlm().getApiKey() == null) {
            log.debug("LLM parsing disabled or no API key");
            return parse(description);
        }

        try {
            String prompt = buildPrompt(description);
            String response = callLlm(prompt);
            return parseLlmResponse(response, description);
        } catch (Exception e) {
            log.warn("LLM parsing failed, falling back to regex: {}", e.getMessage());
            return parse(description);
        }
    }

    private String extractPhone(String text) {
        Matcher matcher = PHONE_PATTERN.matcher(text);
        if (matcher.find()) {
            return normalizePhone(matcher.group());
        }
        return null;
    }

    private String extractName(String text, String phone) {
        String searchText = phone != null ? text.replace(phone, "") : text;
        Matcher matcher = NAME_PATTERN.matcher(searchText);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private String extractComment(String text, String name, String phone) {
        String comment = text;
        if (name != null) comment = comment.replace(name, "");
        if (phone != null) comment = comment.replace(phone, "");

        return comment.replaceAll("[,\\s;.|:-]+$", "").replaceAll("^[,\\s;.|:-]+", "").trim();
    }

    private String normalizePhone(String phone) {
        String digits = phone.replaceAll("[^\\d]", "");
        if (digits.startsWith("8") && digits.length() == 11) {
            return "+7" + digits.substring(1);
        }
        if (digits.startsWith("7") && digits.length() == 11) {
            return "+" + digits;
        }
        if (digits.length() == 10) {
            return "+7" + digits;
        }
        return phone;
    }

    private double calculateConfidence(ParsedEvent parsed) {
        double score = 0;
        if (parsed.getPhone() != null) score += 0.4;
        if (parsed.getName() != null) score += 0.4;

        return Math.min(score, 1.0);
    }

    private String buildPrompt(String description) {
        return """
                Извлеки из текста записи в календаре данные для заказа автодитейлинга.
                в ответе должен быть только JSON без лишнего текста и без ```json ```.
                
                Поля: name (имя клиента), phone (телефон в формате +375* или 80*), car, comment.
                Также нужно извлечь типы работ и их стоимость в  листе works с полями type, cost.
                типы работ сопоставить по  возможности со справочником %s иначе присвоить тип Прочее
                Если поля нет - null.
                
                Текст: "%s"
                """.formatted(dictionaryService.getActiveDictionaryItemsByType("WORK_TYPE"), description);
    }

    private String callLlm(String prompt) {
        String url = properties.getLlm().getBaseUrl() + "/chat/completions";

        var requestBody = objectMapper.createObjectNode()
                .put("model", properties.getLlm().getModel())
                .put("temperature", 0.1)
                .set("messages", objectMapper.createArrayNode().add(
                        objectMapper.createObjectNode()
                                .put("role", "user")
                                .put("content", prompt)
                ));

        return webClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + properties.getLlm().getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private ParsedEvent parseLlmResponse(String response, String originalDescription) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            log.info(content);
            JsonNode parsed = objectMapper.readTree(content);
            ParsedEvent event = new ParsedEvent();
            event.setName(parsed.path("name").asText(null));
            event.setPhone(parsed.path("phone").asText(null));
            event.setCar(parsed.path("car").asText(null));
            event.setComment(parsed.path("comment").asText(null));
            event.setConfidence(0.9);
            return event;
        } catch (Exception e) {
            log.warn("Failed to parse LLM response: {}", e.getMessage());
            return parse(originalDescription);
        }
    }

    @lombok.Data
    public static class ParsedEvent {
        private String name;
        private String phone;
        private String car;
        private String comment;
        private double confidence;
    }
}