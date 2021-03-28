package de.raidcraft.quests.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import de.raidcraft.api.conversations.Conversations;
import de.raidcraft.quests.QuestPlugin;
import de.raidcraft.quests.api.quest.Quest;
import de.raidcraft.quests.api.holder.QuestHolder;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * @author Silthus
 */
public class PlayerCommands {

    private final QuestPlugin plugin;

    public PlayerCommands(QuestPlugin plugin) {

        this.plugin = plugin;
    }

    @Command(
            aliases = {"list"},
            desc = "List all active quests"
    )
    public void list(CommandContext args, CommandSender sender) throws CommandException {

        QuestHolder player = plugin.getQuestManager().getQuestHolder((Player) sender);
        String questList = "";
        for (Quest quest : player.getAllQuests()) {
            if (!questList.isEmpty()) {
                questList += ChatColor.GREEN + ", ";
            }
            questList += ChatColor.RESET;

            if (quest.isActive()) {
                questList += ChatColor.YELLOW + quest.getFriendlyName();
            }
            if (quest.isCompleted()) {
                questList += ChatColor.RED.toString() + ChatColor.STRIKETHROUGH + quest.getFriendlyName();
            }
        }

        if (questList.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Du hast keine noch keine Quest angenommen!");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Du hast folgende Quests:");
            sender.sendMessage(questList);
        }
    }

    @Command(
            aliases = {"abort", "abbrechen", "quit", "exit"},
            desc = "Bricht die angegebene Quest ab.",
            min = 1,
            usage = "<Quest Name>"
    )
    public void abort(CommandContext args, CommandSender sender) throws CommandException {

        Player player = (Player) sender;
        QuestHolder questHolder = plugin.getQuestManager().getQuestHolder(player);
        if (questHolder == null) throw new CommandException("Es ist ein Fehler aufgetreten: QuestHolder not found!");
        Optional<Quest> optionalQuest = questHolder.getQuest(args.getJoinedStrings(0));

        if (!optionalQuest.isPresent()) {
            throw new CommandException("Es gibt keine Quest mit dem Namen: " + args.getJoinedStrings(0));
        }

        Quest quest = optionalQuest.get();
        if (!quest.getTemplate().isAbortable() && !sender.hasPermission("rcquests.admin.abort")) {
            throw new CommandException("Die Quest " + quest.getFriendlyName() + " kann nicht abgebrochen werden.");
        }

        Conversations.askYesNo(player, "Ja, ich möchte die Quest abbrechen.", "Nein, ich behalte die Quest.", result -> {
            if (result) quest.abort();
        }, "Möchtest du die Quest " + quest.getFriendlyName() + " wirklich abbrechen?");
    }
}
