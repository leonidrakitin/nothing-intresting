package ru.sushi.delivery.kds.domain.util;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class Broadcaster{

    public interface BroadcastListener {
        void receiveBroadcast(String message);
    }

    // Храним всех подписчиков (ChefScreenView)
    private static final Set<BroadcastListener> listeners = new CopyOnWriteArraySet<>();

    public static void register(BroadcastListener listener) {
        listeners.add(listener);
    }

    public static void unregister(BroadcastListener listener) {
        listeners.remove(listener);
    }

    public static void broadcast(String message) {
        for (BroadcastListener listener : listeners) {
            listener.receiveBroadcast(message);
        }
    }
}

