package de.raidcraft.quests.listener;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.ItemType;
import de.raidcraft.api.tags.TTag;
import de.raidcraft.quests.QuestPlugin;
import de.raidcraft.quests.QuestPool;
import de.raidcraft.quests.api.events.*;
import de.raidcraft.quests.api.holder.QuestHolder;
import de.raidcraft.quests.util.QuestUtil;
import de.raidcraft.util.CustomItemUtil;
import de.raidcraft.util.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

/**
 * @author Silthus
 */
public class PlayerListener implements Listener {

    private final QuestPlugin plugin;

    public PlayerListener(QuestPlugin plugin) {

        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {

        plugin.getQuestManager().getQuestHolder(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {

        QuestHolder holder = plugin.getQuestManager().clearPlayerCache(event.getPlayer().getUniqueId());
        if (holder != null) {
            holder.save();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onItemPickup(PlayerPickupItemEvent event) {

        if (RaidCraft.isCustomItem(event.getItem().getItemStack())) {
            CustomItemStack customItem = RaidCraft.getCustomItem(event.getItem().getItemStack());
            if (customItem.getItem().getType() == ItemType.QUEST) {
                plugin.getQuestManager().getQuestHolder(event.getPlayer()).getQuestInventory().addItem(customItem);
                FancyMessage msg = new FancyMessage("Du hast das Quest Item ").color(ChatColor.DARK_AQUA);
                msg = CustomItemUtil.getFormattedItemTooltip(msg, customItem);
                msg.then(" in dein ").color(ChatColor.DARK_AQUA)
                        .then("Quest Inventar")
                        .color(ChatColor.AQUA)
                        .tooltip("Klicke hier um dein Quest Inventar zu öffnen.",
                                "Oder gebe /qi ein.")
                        .command("/qi")
                        .then(" aufgenommen.").color(ChatColor.DARK_AQUA)
                        .send(event.getPlayer());
                event.getItem().remove();
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onObjectiveStarted(ObjectiveStartedEvent event) {

        if (event.getObjective().getObjectiveTemplate().isSilent() || event.getObjective().isHidden()) return;
        if (event.getObjective().getQuest().getObjectives().get(0).equals(event.getObjective())) return;

        FancyMessage message = QuestUtil.getQuestTag(event.getObjective().getQuest())
                .text(": Aufgabe ").color(ChatColor.YELLOW);

        message.append(QuestUtil.getQuestObjectiveTag(event.getObjective()));

        message.text(" angenommen.").color(ChatColor.YELLOW);

        event.getObjective().getQuest().getPlayer().spigot().sendMessage(message.create());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onObjectiveComplete(ObjectiveCompletedEvent event) {
        if (event.getObjective().getObjectiveTemplate().isSilent() || event.getObjective().isHidden()) return;

        FancyMessage message = QuestUtil.getQuestTag(event.getObjective().getQuest())
                .text(": Aufgabe ").color(ChatColor.YELLOW);

        message.append(QuestUtil.getQuestObjectiveTag(event.getObjective()));

        message.text(" abgeschlossen.").color(ChatColor.YELLOW);

        event.getObjective().getQuest().getPlayer().spigot().sendMessage(message.create());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onQuestStart(QuestStartedEvent event) {

        TTag.findOrCreateTag("quest-start:" + event.getQuest().getFullName(),
                "Quest START: " + event.getQuest().getFriendlyName() + " (" + event.getQuest().getFullName() + ")", true);

        FancyMessage text = new FancyMessage("Quest").color(ChatColor.YELLOW)
                .text(" [").color(ChatColor.DARK_GRAY)
                .text(event.getQuest().getFriendlyName()).color(ChatColor.GREEN)
                .formattedTooltip(QuestUtil.getQuestTooltip(event.getQuest()))
                .text("]").color(ChatColor.DARK_GRAY)
                .text(" angenommen.").color(ChatColor.YELLOW);
        event.getQuest().getPlayer().spigot().sendMessage(text.create());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onQuestComplete(QuestCompletedEvent event) {

        QuestUtil.getQuestTag(event.getQuest()).then(" erfolgreich abgeschlossen.").color(ChatColor.GREEN)
                .send(event.getQuest().getPlayer());

        if (event.getQuest().getTemplate().isSilent()) return;
        FancyMessage msg = new FancyMessage(event.getQuest().getPlayer().getName()).color(ChatColor.AQUA)
                .then(" hat die Quest ").color(ChatColor.YELLOW);
        msg = QuestUtil.getQuestTooltip(msg, event.getQuest()).then(" abgeschlossen ").color(ChatColor.YELLOW);

        event.getQuest().getPlayer().spigot().sendMessage(msg.create());
    }

    @EventHandler
    public void onQuestAbort(QuestAbortEvent event) {

        if (event.getQuest().getTemplate().isAbortable()) return;
        event.setCancelled(!event.getQuest().getPlayer().hasPermission("rcquests.admin.abort"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onQuestAborted(QuestAbortedEvent event) {

        TTag.findOrCreateTag("quest-abort:" + event.getQuest().getFullName(),
                "Quest ABORTED: " + event.getQuest().getFriendlyName() + " (" + event.getQuest().getFullName() + ")", true);

        if (event.getQuest().getTemplate().isSilent()) return;
        FancyMessage msg = new FancyMessage("Die Quest ").color(ChatColor.RED);
        QuestUtil.getQuestTooltip(msg, event.getQuest())
                .then(" wurde abgebrochen.").color(ChatColor.RED)
                .send(event.getQuest().getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onQuestCompleted(QuestPoolQuestCompletedEvent event) {

        TTag.findOrCreateTag("quest-complete:" + event.getQuest().getFullName(),
                "Quest COMPLETE: " + event.getQuest().getFriendlyName() + " (" + event.getQuest().getFullName() + ")", true);

        Optional<QuestPool> questPool = plugin.getQuestManager().getQuestPool(event.getQuestPool().getQuestPool());
        questPool.ifPresent(questPool1 -> questPool1.getRewardActions()
                .forEach(playerAction -> playerAction.accept(event.getQuest().getPlayer())));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTaskCompletion(TaskCompletedEvent event) {

        if (event.getTask().getTaskTemplate().isSilent() || event.getTask().isHidden()) return;

        FancyMessage message = QuestUtil.getQuestTag(event.getTask().getQuest());
        message.then(": Task ").color(ChatColor.YELLOW)
                .append(QuestUtil.getQuestTaskTag(event.getTask()))
                .then(" abgeschlossen.").color(ChatColor.YELLOW);

        event.getPlayer().spigot().sendMessage(message.create());
    }
}
