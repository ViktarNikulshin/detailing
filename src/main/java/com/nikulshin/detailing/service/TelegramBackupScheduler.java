package com.nikulshin.detailing.service; // Укажите ваш package

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class TelegramBackupScheduler {

    // Рекомендуется вынести эти настройки в src/main/resources/application.properties
    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${db.container.name}")
    private String containerName;

    @Value("${db.name}")
    private String dbName;

    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(cron = "${app.dump.cron}")
    public void executeDailyBackup() {
        log.info("Запуск процедуры ежедневного резервного копирования...");

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String localDirPath = System.getProperty("user.home") + File.separator + "detailing_backups";
        String backupFilePath = localDirPath + File.separator + "backup_" + dateStr + ".dump";

        // 1. Создаем локальную директорию, если её нет
        File backupDir = new File(localDirPath);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        File backupFile = new File(backupFilePath);

        // 2. Формируем процесс выполнения pg_dump внутри Docker
        // Используем бинарный формат (-F c) и выгружаем только данные (--data-only)
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "exec", "-i", containerName,
                "pg_dump", "-U", dbUser, "-d", dbName, "--data-only", "-F", "c"
        );

        // Перенаправляем бинарный поток вывода напрямую в файл (Windows не испортит кодировку)
        processBuilder.redirectOutput(backupFile);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0 && backupFile.exists() && backupFile.length() > 0) {
                log.info("Дамп успешно создан на диске. Отправка в Telegram...");

                // 3. Отправляем в Telegram
                sendToTelegram(backupFile, dateStr);

                // 4. Очищаем локальный диск, чтобы файлы не копились вечно
                if (backupFile.delete()) {
                    log.info("Временный локальный файл бэкапа удален.");
                }
            } else {
               log.error("Сбой при генерации дампа pg_dump. Код выхода: {}", exitCode);
            }

        } catch (IOException | InterruptedException e) {
            log.error("Критическая ошибка при выполнении бэкапа: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Отправка файла дампа через Telegram Bot API
     */
    private void sendToTelegram(File file, String dateStr) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendDocument";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("chat_id", chatId);
        body.add("document", new FileSystemResource(file));
        body.add("caption", "📦 *Авто-бэкап CRM Detailing*\n📅 Дата: " + dateStr + "\n✅ Только данные (--data-only)");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Бэкап успешно доставлен в Telegram-канал.");
            } else {
                System.err.println("Telegram вернул статус-код: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Ошибка при отправке HTTP-запроса в Telegram: " + e.getMessage());
        }
    }
}