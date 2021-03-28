package de.raidcraft.quests.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import de.raidcraft.api.player.UnknownPlayerException;
import de.raidcraft.api.quests.QuestException;
import de.raidcraft.quests.QuestPlugin;
import de.raidcraft.quests.api.holder.QuestHolder;
import de.raidcraft.quests.api.quest.Quest;
import de.raidcraft.quests.api.quest.QuestTemplate;
import de.raidcraft.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Silthus
 */
public class AdminCommands {

    private final QuestPlugin plugin;

    public AdminCommands(QuestPlugin plugin) {

        this.plugin = plugin;
    }

    @Command(
            aliases = {"reload"},
            desc = "Reloads the quest plugin"
    )
    @CommandPermissions("rcquests.admin.reload")
    public void reload(CommandContext args, CommandSender sender) {

        plugin.reload();
        sender.sendMessage(ChatColor.GREEN + "Reloaded Quest Plugin sucessfully!");
    }

    @Command(
            aliases = {"accept", "start", "a"},
            desc = "Starts a quest",
            usage = "<Quest>",
            min = 1
    )
    @CommandPermissions("rcquests.admin.accept")
    public void accept(CommandContext args, CommandSender sender) throws CommandException {

        try {
            QuestTemplate questTemplate = plugin.getQuestManager().getQuestTemplate(args.getString(0));
            QuestHolder player = plugin.getQuestManager().getQuestHolder((Player) sender);
            if (questTemplate == null) {
                throw new CommandException("Unknown quest: " + args.getString(0));
            }
            player.startQuest(questTemplate);
        } catch (QuestException e) {
            throw new CommandException(e);
        }
    }

    @Command(
            aliases = {"abort", "abbruch", "abrechen", "cancel"},
            desc = "Cancels the quest",
            min = 1,
            flags = "p:",
            usage = "[-p <Player>] <Quest>"
    )
    @CommandPermissions("rcquests.admin.abort")
    public void abort(CommandContext args, CommandSender sender) throws CommandException {

        Player targetPlayer = args.hasFlag('p') ? CommandUtil.grabPlayer(args.getFlag('p')) : (Player) sender;
        String questName = args.getJoinedStrings(0);
        if (targetPlayer == null) {
            throw new CommandException("Der angegebene Spieler ist nicht Online!");
        }
        try {
            QuestHolder questPlayer = plugin.getQuestManager().getQuestHolder(targetPlayer);
            Quest quest = plugin.getQuestManager().findQuest(questPlayer, questName);
            if (quest.isActive()) {
                quest.abort();
                quest.delete();
                plugin.getQuestManager().clearCache(questPlayer.getPlayer());
                sender.sendMessage(ChatColor.GREEN + "Die Quest '" + quest.getFriendlyName() + "' wurde abgebrochen!");
            } else {
                throw new CommandException("Quest " + questName + " ist nicht aktiv und kann nicht abgebrochen werden!");
            }
        } catch (QuestException e) {
            throw new CommandException(e.getMessage());
        }
    }

    @Command(
            aliases = {"delete", "remove", "del"},
            desc = "Removes the quest",
            min = 1,
            flags = "p:",
            usage = "[-p <Player>] <Quest>"
    )
    @CommandPermissions("rcquests.admin.remove")
    public void remove(CommandContext args, CommandSender sender) throws CommandException {

        Player targetPlayer = args.hasFlag('p') ? CommandUtil.grabPlayer(args.getFlag('p')) : (Player) sender;
        String questName = args.getJoinedStrings(0);
        if (targetPlayer == null) {
            throw new CommandException("Der angegebene Spieler ist nicht Online!");
        }
        try {
            QuestHolder questPlayer = plugin.getQuestManager().getQuestHolder(targetPlayer);
            Quest quest = plugin.getQuestManager().findQuest(questPlayer, questName);
            if (!quest.isCompleted()) {
                throw new CommandException("Die Quest " + quest.getFriendlyName() + " wurde noch nicht abgeschlossen!");
            }
            quest.delete();
            plugin.getQuestManager().clearCache(questPlayer.getPlayer());
            sender.sendMessage(ChatColor.GREEN + "Die Quest '" + quest.getFriendlyName() + "' wurde entfernt!");
        } catch (QuestException e) {
            throw new CommandException(e.getMessage());
        }
    }

    @Command(
            aliases = {"purge"},
            desc = "Purges all quest history of the player.",
            usage = "[Player]",
            flags = "r:t",
            help = "Use the -r flag to purge quests of a specific region only.\nSet the -t flag to teleport the player to the start location of the region."
    )
    @CommandPermissions("rcquests.admin.purge")
    public void purge(CommandContext args, CommandSender sender) throws CommandException {

        try {
            OfflinePlayer offlinePlayer = CommandUtil.grabOfflinePlayer(args.getString(0, sender.getName()));
            plugin.getQuestManager().purgePlayerHistory(
                    offlinePlayer.getUniqueId(),
                    plugin.getQuestManager().getQuestRegion(args.getFlag('r')).orElse(null),
                    args.hasFlag('t')
            );
            sender.sendMessage(ChatColor.GREEN + "Purged Quest History of " + offlinePlayer.getName() + ".");
        } catch (UnknownPlayerException e) {
            throw new CommandException(e);
        }
    }

    @Command(
            aliases = {"clearinv"},
            desc = "Clears the quest inventory of the player.",
            min = 0,
            usage = "[Player]"
    )
    @CommandPermissions("rcquests.admin.clearinv")
    public void clearinv(CommandContext args, CommandSender sender) throws CommandException {

        try {
            OfflinePlayer offlinePlayer = CommandUtil.grabOfflinePlayer(args.getString(0, sender.getName()));
            QuestHolder questHolder = plugin.getQuestManager().getPlayer(offlinePlayer.getUniqueId());
            if (questHolder == null)
                throw new CommandException("Player " + args.getString(0) + " is not a quest player!");
            questHolder.getQuestInventory().clear();
            sender.sendMessage(ChatColor.GREEN + "Purged Quest Inventory of " + offlinePlayer.getName() + ". Please login and out again.");
        } catch (UnknownPlayerException e) {
            throw new CommandException(e);
        }
    }
}
