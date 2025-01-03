package ru.sushi.delivery.kds.service;

import ru.sushi.delivery.kds.service.dto.BroadcastMessage;
import ru.sushi.delivery.kds.service.listeners.BroadcastListener;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class AbstractBroadcasterService {

    private final Set<BroadcastListener> listeners = new CopyOnWriteArraySet<>();

    public void register(BroadcastListener listener) {
        listeners.add(listener);
    }

    public void unregister(BroadcastListener listener) {
        listeners.remove(listener);
    }

    public void broadcast(BroadcastMessage message) {
        for (BroadcastListener listener : listeners) {
            listener.receiveBroadcast(message);
        }
    }
}

