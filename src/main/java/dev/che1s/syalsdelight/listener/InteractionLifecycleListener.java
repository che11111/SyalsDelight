package dev.che1s.syalsdelight.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class InteractionLifecycleListener implements Listener {
    private final HandInteractionDeduplicator deduplicator;

    public InteractionLifecycleListener(HandInteractionDeduplicator deduplicator) {
        this.deduplicator = deduplicator;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        deduplicator.forget(event.getPlayer().getUniqueId());
    }
}

