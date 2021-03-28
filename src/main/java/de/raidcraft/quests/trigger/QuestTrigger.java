package de.raidcraft.quests.trigger;

import de.raidcraft.api.action.trigger.Trigger;
import de.raidcraft.quests.api.events.QuestCompleteEvent;
import de.raidcraft.quests.api.events.QuestCompletedEvent;
import de.raidcraft.quests.api.events.QuestStartedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * @author mdoering
 */
public class QuestTrigger extends Trigger implements Listener {

    public QuestTrigger() {

        super("quest", "complete", "completed", "started");
    }

    @Information(
            value = "quest.started",
            desc = "Is triggered when the quest was started.",
            conf = {
                    "quest: <id>"
            }
    )
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onQuestStart(QuestStartedEvent event) {

        informListeners("started", event.getQuest().getPlayer(), config -> {
            if (config.isSet("quest") && !event.getQuest().getFullName().equalsIgnoreCase(config.getString("quest"))) return false;
            return true;
        });
    }

    @Information(
            value = "quest.complete",
            desc = "Is triggered when the quest is about to complete, before rewards and messages are issued.",
            conf = {
                    "quest: <id>"
            }
    )
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onQuestComplete(QuestCompleteEvent event) {

        informListeners("complete", event.getQuest().getPlayer(), config -> {
            if (config.isSet("quest") && !event.getQuest().getFullName().equalsIgnoreCase(config.getString("quest"))) return false;
            return true;
        });
    }

    @Information(
            value = "quest.completed",
            desc = "Is triggered when the quest was completed and after messages and rewards have been issued.",
            conf = {
                    "quest: <id>"
            }
    )
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onQuestCompleted(QuestCompletedEvent event) {

        informListeners("completed", event.getQuest().getPlayer(), config -> {
            if (config.isSet("quest") && !event.getQuest().getFullName().equalsIgnoreCase(config.getString("quest"))) return false;
            return true;
        });
    }
}
