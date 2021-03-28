package de.raidcraft.quests.requirements;

import de.raidcraft.api.action.requirement.Requirement;
import de.raidcraft.quests.QuestManager;
import de.raidcraft.quests.api.holder.QuestHolder;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

@Data
public class HasQuestItemRequirement implements Requirement<Player> {

    private final QuestManager questManager;

    @Information(
            value = "quest.item.has",
            aliases = {"quest.has-item"},
            desc = "Checks if the player has the given quest item in his quest inventory.",
            conf = {
                    "item: <rc123,this.quest-item>"
            }
    )
    @Override
    public boolean test(Player player, ConfigurationSection config) {
        QuestHolder holder = getQuestManager().getQuestHolder(player);
        return holder != null && holder.getQuestInventory().contains(config.getString("item"));
    }
}
