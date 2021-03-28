package de.raidcraft.quests.api.events;

import de.raidcraft.quests.api.quest.Quest;
import de.raidcraft.quests.tables.TPlayerQuestPool;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author mdoering
 */
@Getter
@Setter
@RequiredArgsConstructor
public class QuestPoolQuestAbortedEvent extends Event {

    private final Quest quest;
    private final TPlayerQuestPool questPool;

    //<-- Handler -->//
    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {

        return handlers;
    }

    public static HandlerList getHandlerList() {

        return handlers;
    }
}
