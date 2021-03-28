package de.raidcraft.quests.trigger;

import de.raidcraft.api.action.trigger.Trigger;
import de.raidcraft.quests.api.events.ObjectiveCompleteEvent;
import de.raidcraft.quests.api.events.ObjectiveCompletedEvent;
import de.raidcraft.quests.api.events.ObjectiveStartedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * @author mdoering
 */
public class ObjectiveTrigger extends Trigger implements Listener {

    public ObjectiveTrigger() {

        super("objective", "complete", "completed", "started");
    }

    @Information(
            value = "objective.started",
            desc = "Is triggered when the objective was started.",
            conf = {
                    "quest: <id>"
            }
    )
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onObjectiveStart(ObjectiveStartedEvent event) {

        informListeners("started", event.getObjective().getQuest().getPlayer(),
                config -> {
                    if (config.isSet("objective") && event.getObjective().getId() != config.getInt("objective")) {
                        return false;
                    }
                    if (config.isSet("quest") && !event.getObjective().getQuest().getFullName().equals(config.getString("quest"))) {
                        return false;
                    }
                    return true;
                }
        );
    }

    @Information(
            value = "objective.complete",
            desc = "Is triggered before the objective gets completed and before messages are shown.",
            conf = {
                    "quest: <id>",
                    "objective: <int:id>"
            }
    )
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onObjectiveComplete(ObjectiveCompleteEvent event) {

        informListeners("complete", event.getObjective().getQuest().getPlayer(),
                config -> {
                    if (config.isSet("objective") && event.getObjective().getId() != config.getInt("objective")) {
                        return false;
                    }
                    if (config.isSet("quest") && !event.getObjective().getQuest().getFullName().equals(config.getString("quest"))) {
                        return false;
                    }
                    return true;
                }
        );
    }

    @Information(
            value = "objective.completed",
            desc = "Is triggered after the objective was completed and after messages have been issued.",
            conf = {
                    "quest: <id>",
                    "objective: <int:id>"
            }
    )
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onObjectiveCompleted(ObjectiveCompletedEvent event) {

        informListeners("completed", event.getObjective().getQuest().getPlayer(),
                config -> {
                    if (config.isSet("objective") && event.getObjective().getId() != config.getInt("objective")) {
                        return false;
                    }
                    if (config.isSet("quest") && !event.getObjective().getQuest().getFullName().equals(config.getString("quest"))) {
                        return false;
                    }
                    return true;
                }
        );
    }
}
