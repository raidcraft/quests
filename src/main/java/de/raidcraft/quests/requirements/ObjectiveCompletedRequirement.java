package de.raidcraft.quests.requirements;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.requirement.Requirement;
import de.raidcraft.quests.QuestManager;
import de.raidcraft.quests.api.holder.QuestHolder;
import de.raidcraft.quests.api.quest.Quest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * @author Silthus
 */
public class ObjectiveCompletedRequirement implements Requirement<Player> {

    @Override
    @Information(
            value = "objective.completed",
            desc = "Tests if the player has completed the given objective of the given quest.",
            aliases = {"objective.complete", "obj.complete", "obj.completed"},
            conf = {
                    "quest: <quest id>",
                    "objective: <objective id>"
            }
    )
    public boolean test(Player player, ConfigurationSection config) {

        QuestHolder questHolder = RaidCraft.getComponent(QuestManager.class).getQuestHolder(player);
        if (questHolder == null) return false;
        return questHolder.getQuest(config.getString("quest"))
                .map(quest -> quest.isObjectiveCompleted(config.getInt("objective"))).orElse(false);
    }
}
