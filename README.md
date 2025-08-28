# 📌 Link Tracker

Link Tracker — это система для отслеживания изменения контента и уведомления пользователей через Telegram-бота (пока что поддерживается только Github и StackOverflow).  
Проект состоит из двух сервисов:

- **Bot** — Telegram-бот для взаимодействия с пользователями.
- **Scrapper** — сервис для хранения, обработки и обновления данных о ссылках.

---

## 🚀 Возможности

- Подписка на ссылки и их отслеживание.
- Хранение информации в PostgreSQL.
- Асинхронная обработка событий с помощью Kafka.
- Кэширование в Redis.
- Интеграция с Prometheus и OpenTelemetry для метрик и мониторинга.
- REST API с OpenAPI документацией.
- Надёжность с помощью Resilience4j (ретраи, таймауты, fallback).
- Тестирование с использованием Testcontainers.

---

## 📂 Архитектура

Telegram User ↔ Bot ↔ Kafka ↔ Scrapper ↔ PostgreSQL  
↕  
Prometheus / Grafana

- **Bot** — принимает команды от пользователей, сохраняет их в Redis и отправляет события в Kafka.  
- **Scrapper** — обрабатывает события, работает с PostgreSQL, выполняет миграции через Liquibase и публикует обновления.

---

## 🛠️ Технологии

- Java 17  
- Spring Boot 3  
- PostgreSQL + Liquibase  
- Redis  
- Apache Kafka  
- Micrometer + Prometheus + OpenTelemetry  
- Testcontainers  
- Resilience4j  
- Springdoc OpenAPI

---

## ⚙️ Запуск

### 1. Клонирование проекта
    git clone https://github.com/nifreebie/link_tracker.git
    cd link_tracker

### 2. Сборка
    ./mvnw clean package

### 3. Запуск сервисов
Можно запустить каждый сервис отдельно:

    cd bot
    ./mvnw spring-boot:run

    cd scrapper
    ./mvnw spring-boot:run

Или собрать Docker-образы:

    ./mvnw spring-boot:build-image

---

## 📖 API Документация

После запуска Scrapper доступна Swagger-документация:  
http://localhost:8080/swagger-ui.html

---

## 📊 Мониторинг

Метрики Prometheus доступны по адресу:  
http://localhost:8080/actuator/prometheus

Трассировки доступны через OpenTelemetry (совместимо с Jaeger / Grafana Tempo).

---

## 🧪 Тестирование

    ./mvnw test

Тесты используют Testcontainers для изоляции (PostgreSQL, Kafka).

---

## 📜 Лицензия

MIT License © 2025 nifreebie
