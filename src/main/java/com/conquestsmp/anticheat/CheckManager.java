
package com.conquestsmp.anticheat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CheckManager {
    private final ConquestAntiCheat plugin;
    private final Map<UUID, Integer> speedFlags = new HashMap<>();
    private final Map<UUID, Integer> flightFlags = new HashMap<>();
    private final Map<String, Long> alertTimes = new HashMap<>();

    public CheckManager(ConquestAntiCheat plugin) {
        this.plugin = plugin;
    }

    public int speedFlag(UUID id) {
        return speedFlags.merge(id, 1, Integer::sum);
    }

    public int flightFlag(UUID id) {
        return flightFlags.merge(id, 1, Integer::sum);
    }

    public boolean shouldAlert(UUID id, String check, long cooldownSeconds) {
        String key = id + ":" + check;
        long now = System.currentTimeMillis();
        long last = alertTimes.getOrDefault(key, 0L);

        if (now - last < cooldownSeconds * 1000L) {
            return false;
        }

        alertTimes.put(key, now);
        return true;
    }

    public void reset(UUID id) {
        speedFlags.remove(id);
        flightFlags.remove(id);
        alertTimes.keySet().removeIf(
            key -> key.startsWith(id.toString() + ":")
        );
    }

    public ConquestAntiCheat plugin() {
        return plugin;
    }
}
