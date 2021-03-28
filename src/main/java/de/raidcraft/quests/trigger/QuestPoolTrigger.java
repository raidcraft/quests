package de.raidcraft.quests.trigger;

import de.raidcraft.api.action.trigger.Trigger;
import de.raidcraft.quests.api.events.QuestPoolQuestAbortedEvent;
import de.raidcraft.quests.api.events.QuestPoolQuestCompletedEvent;
import de.raidcraft.quests.api.events.QuestPoolQuestStartedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * @author mdoering
 */
public class QuestPoolTrigger extends Trigger implements Listener {

    public QuestPoolTrigger() {

        super("questpool", "quest.aborted", "quest.completed", "quest.started");
    }

    @Information(
            value = "questpool.quest.started",
            desc = "Is triggered when a quest of a quest pool was started",
            conf = {
                    "quest: <id>",
                    "pool: <displayName>"
            }
    )
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onQuestStart(QuestPoolQuestStartedEvent event) {

        informListeners("quest.started", event.getQuest().getPlayer(), config -> {
            if (config.isSet("quest") && !event.getQuest().getFullName().equalsIgnoreCase(config.getString("quest"))) return false;
            return !config.isSet("pool") || event.getQuestPool().getQuestPool().equalsIgnoreCase(config.getString("pool"));
        });
    }

    @Information(
            value = "questpool.quest.aborted",
            desc = "Is triggered when the quest is about to complete, before rewards and messages are issued.",
            conf = {
                    "quest: <id>",
                    "pool: <displayName>"
            }
    )
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onQuestAborted(QuestPoolQuestAbortedEvent event) {

        informListeners("quest.aborted", event.getQuest().getPlayer(), config -> {
            if (config.isSet("quest") && !event.getQuest().getFullName().equalsIgnoreCase(config.getString("quest"))) return false;
            return !config.isSet("pool") || event.getQuestPool().getQuestPool().equalsIgnoreCase(config.getString("pool"));
        });
    }

    @Information(
            value = "questpool.quest.completed",
            desc = "Is triggered when the quest was completed and after messages and rewards have been issued.",
            conf = {
                    "quest: <id>",
                    "pool: <displayName>"
            }
    )
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onQuestCompleted(QuestPoolQuestCompletedEvent event) {

        informListeners("quest.completed", event.getQuest().getPlayer(), config -> {
            if (config.isSet("quest") && !event.getQuest().getFullName().equalsIgnoreCase(config.getString("quest"))) return false;
            return !config.isSet("pool") || event.getQuestPool().getQuestPool().equalsIgnoreCase(config.getString("pool"));
        });
    }
}
