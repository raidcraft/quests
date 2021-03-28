package de.raidcraft.quests.api.events;

import de.raidcraft.quests.api.objective.PlayerObjective;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author mdoering
 */
@Getter
@Setter
@RequiredArgsConstructor
public class ObjectiveCompleteEvent extends Event implements Cancellable {

    private final PlayerObjective objective;
    private boolean cancelled = false;

    //<-- Handler -->//
    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {

        return handlers;
    }

    public static HandlerList getHandlerList() {

        return handlers;
    }
}
