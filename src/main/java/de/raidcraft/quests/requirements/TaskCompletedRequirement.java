package de.raidcraft.quests.requirements;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.requirement.Requirement;
import de.raidcraft.quests.QuestManager;
import de.raidcraft.quests.api.holder.QuestHolder;
import de.raidcraft.quests.api.quest.Quest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Optional;

public class TaskCompletedRequirement implements Requirement<Player> {

    @Information(
            value = "task.completed",
            aliases = {"task.complete"},
            desc = "Tests if the player has completed the given task.",
            conf = {
                    "quest: id of the quest",
                    "objective: id of the objective the task belongs to",
                    "task: id of the task"
            }
    )
    @Override
    public boolean test(Player player, ConfigurationSection config) {
        QuestHolder questHolder = RaidCraft.getComponent(QuestManager.class).getQuestHolder(player);
        if (questHolder == null) return false;

        return questHolder.getQuest(config.getString("quest"))
                .map(quest -> quest.isTaskCompleted(config.getInt("objective"), config.getInt("task")))
                .orElse(false);
    }
}
