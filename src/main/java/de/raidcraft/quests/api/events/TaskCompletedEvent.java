package de.raidcraft.quests.api.events;

import de.raidcraft.quests.api.objective.PlayerTask;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author mdoering
 */
@Getter
@Setter
@RequiredArgsConstructor
public class TaskCompletedEvent extends Event {

    private final PlayerTask task;

    public Player getPlayer() {
        return getTask().getQuest().getPlayer();
    }

    //<-- Handler -->//
    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {

        return handlers;
    }

    public static HandlerList getHandlerList() {

        return handlers;
    }
}
