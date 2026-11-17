package com.nikulshin.detailing.service.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikulshin.detailing.configuration.GoogleCalendarProperties;
import com.nikulshin.detailing.service.DictionaryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
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

    /** Belarusian numbers in international (+375) or national (80) format. */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?<!\\d)(?:\\+?375|8[\\s\\-\\(]*0)"
                    + "[\\s\\-\\(\\)\\.]*\\d{2}"
                    + "[\\s\\-\\(\\)\\.]*\\d{3}"
                    + "[\\s\\-\\(\\)\\.]*\\d{2}"
                    + "[\\s\\-\\(\\)\\.]*\\d{2}(?!\\d)"
    );

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "(?:^|\\s)([А-ЯЁ][а-яё]+(?:\\s+[А-ЯЁ][а-яё]+){1,2})(?:\\s|$|,|;|\\.|:|\\||-)",
            Pattern.UNICODE_CHARACTER_CLASS
    );

    /**
     * Parses an event according to the order policy:
     * phone/name are extracted locally first; the LLM is always called afterwards.
     * If both fields are already known, the LLM is asked only for works and optional
     * enrichment. If either field is missing, the LLM parses the complete event.
     */
    public ParsedEvent parseForOrder(String description) {
        String safeDescription = description == null ? "" : description;
        ParsedEvent regexParsed = parse(safeDescription);

        if (regexParsed.getPhone() != null && regexParsed.getName() != null) {
            ParsedEvent llmParsed = parseWithLlm(safeDescription, false);
            llmParsed.setPhone(regexParsed.getPhone());
            llmParsed.setName(regexParsed.getName());
            if (llmParsed.getComment() == null || llmParsed.getComment().isBlank()) {
                llmParsed.setComment(regexParsed.getComment());
            }
            llmParsed.setConfidence(calculateConfidence(llmParsed));
            return llmParsed;
        }

        ParsedEvent llmParsed = parseWithLlm(safeDescription, true);
        if (llmParsed.getPhone() == null) {
            llmParsed.setPhone(regexParsed.getPhone());
        }
        if (llmParsed.getName() == null) {
            llmParsed.setName(regexParsed.getName());
        }
        llmParsed.setConfidence(calculateConfidence(llmParsed));
        return llmParsed;
    }

    /** Free parser used as the first pass and as an LLM fallback. */
    public ParsedEvent parse(String description) {
        ParsedEvent parsed = new ParsedEvent();
        String safeDescription = description == null ? "" : description;
        String withoutPhone = safeDescription;

        Matcher phoneMatcher = PHONE_PATTERN.matcher(safeDescription);
        if (phoneMatcher.find()) {
            parsed.setPhone(normalizePhone(phoneMatcher.group()));
            withoutPhone = (safeDescription.substring(0, phoneMatcher.start()) + " "
                    + safeDescription.substring(phoneMatcher.end())).trim();
        }

        Matcher nameMatcher = NAME_PATTERN.matcher(withoutPhone);
        if (nameMatcher.find()) {
            parsed.setName(nameMatcher.group(1).trim());
        }

        String comment = withoutPhone;
        if (parsed.getName() != null) {
            comment = comment.replace(parsed.getName(), "");
        }
        parsed.setComment(cleanComment(comment));
        parsed.setConfidence(calculateConfidence(parsed));
        return parsed;
    }

    /** Backward-compatible full LLM parsing entry point. */
    public ParsedEvent parseWithLlm(String description) {
        return parseWithLlm(description, true);
    }

    private ParsedEvent parseWithLlm(String description, boolean fullExtraction) {
        String apiKey = properties.getLlm().getApiKey();
        boolean ollama = "ollama".equalsIgnoreCase(properties.getLlm().getProvider());
        if (!properties.getLlm().isEnabled() || (isBlank(apiKey) && !ollama)) {
            log.debug("LLM parsing disabled or API key is missing");
            return parse(description);
        }

        try {
            String prompt = buildPrompt(description, fullExtraction);
            String response = callLlm(prompt);
            return parseLlmResponse(response, description);
        } catch (Exception e) {
            log.warn("LLM parsing failed, falling back to regex: {}", e.getMessage());
            return parse(description);
        }
    }

    private String buildPrompt(String description, boolean fullExtraction) {
        String mode = fullExtraction
                ? "Извлеки все поля: name, phone, car, comment и works."
                : "Имя и телефон уже извлечены локально. Не изменяй их. Извлеки works, car и comment.";

        return """
                Ты извлекаешь данные из записи календаря для заказа автодетейлинга.
                Ответь только валидным JSON без markdown и без ```json.
                %s
                Поля JSON:
                - name: имя клиента или null
                - phone: телефон в формате +375XXXXXXXXX или 80XXXXXXXXX или null
                - car: марка/модель автомобиля или null
                - comment: дополнительный комментарий или null
                - works: массив объектов {"type": string, "cost": number|null}
                Используй в works только виды работ из справочника ниже. Если ни одну работу
                уверенно сопоставить нельзя — верни works как пустой массив [].
                Не придумывай работы и стоимость.
                Справочник WORK_TYPE: %s
                Текст записи:
                %s
                """.formatted(mode, dictionaryService.getActiveDictionaryItemsByType("WORK_TYPE"), description);
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
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new IllegalArgumentException("LLM response does not contain choices");
            }

            String content = choices.get(0).path("message").path("content").asText("");
            content = content.replaceFirst("^```(?:json)?\\s*", "")
                    .replaceFirst("\\s*```$", "").trim();
            JsonNode parsed = objectMapper.readTree(content);

            ParsedEvent event = new ParsedEvent();
            event.setName(parsed.path("name").asText(null));
            String llmPhone = parsed.path("phone").asText(null);
            event.setPhone(llmPhone == null ? null : normalizePhone(llmPhone));
            event.setCar(parsed.path("car").asText(null));
            event.setComment(parsed.path("comment").asText(null));

            JsonNode works = parsed.path("works");
            if (works.isArray()) {
                for (JsonNode workNode : works) {
                    String type = workNode.path("type").asText(null);
                    if (type == null || type.isBlank()) {
                        continue;
                    }
                    Integer cost = workNode.path("cost").isNumber()
                            ? workNode.path("cost").asInt()
                            : null;
                    event.getWorks().add(new ParsedWork(type.trim(), cost));
                }
            }
            event.setConfidence(calculateConfidence(event));
            return event;
        } catch (Exception e) {
            log.warn("Failed to parse LLM response: {}", e.getMessage());
            return parse(originalDescription);
        }
    }

    private String normalizePhone(String phone) {
        String digits = phone.replaceAll("[^\\d]", "");
        if (digits.startsWith("375") && digits.length() == 12) {
            return "+" + digits;
        }
        if (digits.startsWith("80") && digits.length() == 11) {
            return "+375" + digits.substring(2);
        }
        return phone;
    }

    private double calculateConfidence(ParsedEvent parsed) {
        double score = 0;
        if (parsed.getPhone() != null) score += 0.4;
        if (parsed.getName() != null) score += 0.4;
        return Math.min(score, 1.0);
    }

    private String cleanComment(String comment) {
        return comment.replaceAll("[,\\s;.|:-]+$", "")
                .replaceAll("^[,\\s;.|:-]+", "")
                .trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Data
    public static class ParsedEvent {
        private String name;
        private String phone;
        private String car;
        private String comment;
        private double confidence;
        private List<ParsedWork> works = new ArrayList<>();
    }

    public record ParsedWork(String type, Integer cost) {
    }
}
