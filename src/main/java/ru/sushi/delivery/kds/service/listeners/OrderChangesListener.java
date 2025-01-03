package ru.sushi.delivery.kds.service.listeners;

import org.springframework.stereotype.Component;
import ru.sushi.delivery.kds.service.dto.BroadcastMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderChangesListener {

    private static final Map<Long, BroadcastListener> listeners = new ConcurrentHashMap<>();

    public void register(long stationId, BroadcastListener listener) {
        listeners.put(stationId, listener);
    }

    public void unregister(long stationId) {
        listeners.remove(stationId);
    }

    public void broadcastAll(BroadcastMessage message) {
        for (BroadcastListener listener : listeners.values()) {
            listener.receiveBroadcast(message);
        }
    }

    public void broadcast(long stationId, BroadcastMessage message) {
        if (listeners.containsKey(stationId)) {
            listeners.get(stationId).receiveBroadcast(message);
        }
    }
}
