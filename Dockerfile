FROM tomcat:11-jdk21-temurin

# Устанавливаем зависимости для добавления репозитория
RUN apt-get update && apt-get install -y wget gnupg2 lsb-release && \
    # Добавляем официальный ключ репозитория PostgreSQL
    install -d /etc/apt/keyrings && \
    wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | gpg --dearmor -o /etc/apt/keyrings/postgresql.gpg && \
    # Добавляем сам репозиторий в источники apt
    echo "deb [signed-by=/etc/apt/keyrings/postgresql.gpg] http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list && \
    # Обновляем списки пакетов и ставим именно 18-ю версию клиента
    apt-get update && apt-get install -y postgresql-client-18 && \
    # Очищаем кэш apt, чтобы образ не весил лишнего
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Создаем папку для бэкапов внутри контейнера и даем права
RUN mkdir -p /var/backups/detailing && chmod 777 /var/backups/detailing