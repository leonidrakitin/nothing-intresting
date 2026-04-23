package ru.sushi.delivery.kds.service;

public record CascadeNotificationResult(boolean telegramSent, boolean vkSent) {

    public boolean anySent() {
        return telegramSent || vkSent;
    }
}
