package de.raidcraft.quests.ui;

import com.google.common.base.Strings;
import de.raidcraft.RaidCraft;
import de.raidcraft.items.commands.BookUtilCommands;
import de.raidcraft.quests.QuestPlugin;
import de.raidcraft.quests.api.objective.PlayerObjective;
import de.raidcraft.quests.api.objective.PlayerTask;
import de.raidcraft.quests.api.quest.Quest;
import de.raidcraft.quests.api.holder.QuestHolder;
import de.raidcraft.quests.util.QuestUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import xyz.upperlevel.spigot.book.BookUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Silthus
 */
public class QuestUI implements Listener {

    public enum Type {

        ACTIVE("Angenommene Aufgaben"),
        CLOSED("Erledigte Aufgaben");

        private final String inventoryName;

        private Type(String inventoryName) {

            this.inventoryName = inventoryName;
        }

        public String getInventoryName() {

            return inventoryName;
        }
    }

    private final QuestHolder holder;
    private final List<Quest> quests;
    private final Type type;
    private final ItemStack questBook;

    public QuestUI(QuestHolder holder, List<Quest> quests, Type type) {

        this.holder = holder;
        this.quests = quests;
        this.type = type;

        BookUtil.BookBuilder book = BookUtil.writtenBook().title("Quest Buch");

        BookUtil.PageBuilder index = BookUtil.PageBuilder.of(BookUtil.TextBuilder.of("Aktive Quests").color(ChatColor.GOLD).build())
                .newLine().newLine();
        ArrayList<BookUtil.PageBuilder> pages = new ArrayList<>();
        for (int i = 0; i < quests.size(); i++) {
            Quest quest = quests.get(i);
            index.add(BookUtil.TextBuilder.of(quest.getFriendlyName()).color(ChatColor.DARK_GRAY).style(ChatColor.UNDERLINE)
                    .onHover(BookUtil.HoverAction.showText(QuestUtil.getQuestTooltip(quest).create()))
                    .onClick(BookUtil.ClickAction.changePage(i + 2))
                    .build()).newLine();
            BookUtil.PageBuilder questText = BookUtil.PageBuilder.of(BookUtil.TextBuilder.of("Zur Übersicht").color(ChatColor.GRAY).style(ChatColor.ITALIC).style(ChatColor.UNDERLINE)
                    .onClick(BookUtil.ClickAction.changePage(1)).build())
                    .newLine();
            if (quest.getTemplate().isAbortable()) {
                questText.add(BookUtil.TextBuilder.of(" [Quest abbrechen?]").color(ChatColor.RED).style(ChatColor.UNDERLINE)
                        .onClick(BookUtil.ClickAction.runCommand("/quest abort " + quest.getFullName())).build());
            }

            questText.newLine().add(QuestUtil.getQuestTag(quest).create());

            questText.newLine();
            questText.newLine();
            for (PlayerObjective objective : quest.getObjectives()) {

                if (objective.isHidden()) continue;

                BookUtil.TextBuilder objText;
                if (objective.isCompleted()) {
                    objText = BookUtil.TextBuilder.of(objective.getObjectiveTemplate().getFriendlyName())
                            .color(ChatColor.GRAY).style(ChatColor.STRIKETHROUGH);
                } else if (objective.isActive()) {
                    objText = BookUtil.TextBuilder.of(objective.getObjectiveTemplate().getFriendlyName())
                            .color(ChatColor.DARK_GRAY);
                } else {
                    objText = BookUtil.TextBuilder.of(objective.getObjectiveTemplate().getFriendlyName())
                            .color(ChatColor.GRAY);
                }

                String description = objective.getObjectiveTemplate().getDescription();
                if (!Strings.isNullOrEmpty(description)) {
                    objText.onHover(BookUtil.HoverAction.showText(BookUtil.TextBuilder.of(description).color(ChatColor.GRAY).build()));
                }

                questText.add(objText.build()).newLine();
            }
            pages.add(questText);
        }

        pages.add(0, index);
        book.pages(pages.stream().map(BookUtil.PageBuilder::build).collect(Collectors.toList()));

        this.questBook = book.build();
    }


    public void open() {

        BookUtil.openPlayer(getHolder().getPlayer(), questBook);
    }

    public void close() {
    }

    public QuestHolder getHolder() {

        return holder;
    }

    public List<Quest> getQuests() {

        return quests;
    }

    public Type getType() {

        return type;
    }
}
