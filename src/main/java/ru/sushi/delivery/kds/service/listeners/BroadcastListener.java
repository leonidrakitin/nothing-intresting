package ru.sushi.delivery.kds.service.listeners;

import ru.sushi.delivery.kds.service.dto.BroadcastMessage;

public interface BroadcastListener {
    void receiveBroadcast(BroadcastMessage message);
}
