package dev.che1s.syalsdelight.listener;

import com.huidu.farmersdelight.api.event.FarmersDelightWarmupEvent;
import dev.che1s.syalsdelight.compat.ContentReadinessVerifier;
import dev.che1s.syalsdelight.compat.OptionalCompatibilityChecker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.concurrent.atomic.AtomicBoolean;

public final class CompatibilityWarmupListener implements Listener {
    private final OptionalCompatibilityChecker checker;
    private final ContentReadinessVerifier verifier;
    private final AtomicBoolean completed = new AtomicBoolean();

    public CompatibilityWarmupListener(
            OptionalCompatibilityChecker checker,
            ContentReadinessVerifier verifier
    ) {
        this.checker = checker;
        this.verifier = verifier;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWarmup(FarmersDelightWarmupEvent event) {
        if (completed.compareAndSet(false, true)) {
            verifier.verify();
            if (checker != null) {
                checker.check();
            }
        }
    }
}

