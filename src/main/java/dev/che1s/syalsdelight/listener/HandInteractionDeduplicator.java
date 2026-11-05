package dev.che1s.syalsdelight.listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HandInteractionDeduplicator {
    private final Map<UUID, InteractionStamp> lastInteractions = new ConcurrentHashMap<>();

    public boolean isDuplicate(
            UUID playerId,
            String exactBlockId,
            int x,
            int y,
            int z,
            long tick
    ) {
        if (playerId == null || exactBlockId == null) {
            return false;
        }
        InteractionStamp current = new InteractionStamp(exactBlockId, x, y, z, tick);
        InteractionStamp previous = lastInteractions.put(playerId, current);
        return current.equals(previous);
    }

    public void forget(UUID playerId) {
        if (playerId != null) {
            lastInteractions.remove(playerId);
        }
    }

    public int cachedPlayers() {
        return lastInteractions.size();
    }

    private record InteractionStamp(
            String exactBlockId,
            int x,
            int y,
            int z,
            long tick
    ) {
    }
}

