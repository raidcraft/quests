package de.raidcraft.quests.ui;

import de.raidcraft.quests.api.objective.PlayerObjective;
import de.raidcraft.quests.api.quest.Quest;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Silthus
 */
public class QuestBook extends ItemStack {

    private final Quest quest;

    protected QuestBook(Quest quest) {

        super(Material.WRITTEN_BOOK, 1);
        this.quest = quest;
        // lets set the title description and so on
        BookMeta meta = (BookMeta) getItemMeta();
        meta.setDisplayName(quest.getFriendlyName());
        if (quest.getAuthors().size() > 0) {
            meta.setAuthor(quest.getAuthors().get(0));
        }
        meta.setTitle(quest.getFriendlyName());
        ArrayList<String> lore = new ArrayList<>();
        if (!Objects.isNull(quest.getDescription())) {
            lore.add(ChatColor.WHITE + quest.getDescription());
        }
        lore.add("");
        // add quest objectives
        for (PlayerObjective objective : quest.getObjectives()) {
            String friendlyName = objective.getObjectiveTemplate().getFriendlyName();
            if (!objective.isHidden()) {
                if (objective.isCompleted()) {
                    lore.add(ChatColor.STRIKETHROUGH + "" + ChatColor.GRAY + friendlyName);
                } else if (objective.getObjectiveTemplate().isOptional()) {
                    lore.add(ChatColor.ITALIC + friendlyName);
                } else {
                    lore.add(ChatColor.WHITE + friendlyName);
                }
            }
        }
        meta.setLore(lore);
        setItemMeta(meta);
    }

    public void open() {

        // TODO: find a way to implement book opening
    }
}
