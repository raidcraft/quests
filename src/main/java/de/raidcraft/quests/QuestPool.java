package de.raidcraft.quests;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.ActionAPI;
import de.raidcraft.api.action.TriggerFactory;
import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.action.trigger.TriggerListener;
import de.raidcraft.api.action.trigger.TriggerListenerConfigWrapper;
import de.raidcraft.api.quests.QuestException;
import de.raidcraft.api.random.GenericRDSTable;
import de.raidcraft.api.random.RDS;
import de.raidcraft.api.random.RDSObject;
import de.raidcraft.quests.api.events.QuestPoolQuestStartedEvent;
import de.raidcraft.quests.api.holder.QuestHolder;
import de.raidcraft.quests.api.quest.Quest;
import de.raidcraft.quests.api.quest.QuestTemplate;
import de.raidcraft.quests.random.RDSQuestObject;
import de.raidcraft.quests.tables.TPlayer;
import de.raidcraft.quests.tables.TPlayerQuest;
import de.raidcraft.quests.tables.TPlayerQuestPool;
import de.raidcraft.util.ConfigUtil;
import io.ebean.EbeanServer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author mdoering
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuestPool extends GenericRDSTable implements TriggerListener<Player> {

    private final boolean enabled;
    private final String name;
    private final String friendlyName;
    private final int maxActiveQuests;
    private final double cooldown;
    private final boolean abortPrevious;
    private final Optional<LocalTime> resetTime;
    private final List<TriggerFactory> triggers = new ArrayList<>();
    private final List<Action<Player>> rewardActions = new ArrayList<>();

    protected QuestPool(String name, ConfigurationSection config) {

        super(null, config.getInt("count", 1), config.getDouble("probability", 1.0));
        this.name = name;
        this.enabled = config.getBoolean("enabled", true);
        this.friendlyName = config.isSet("name") ? config.getString("name") : name;
        this.maxActiveQuests = config.getInt("max-active-quests", 1);
        this.abortPrevious = config.getBoolean("abort-previous-quests", false);
        // 24h
        this.cooldown = config.getDouble("cooldown", 86400);
        if (config.isSet("reset-time")) {
            this.resetTime = Optional.ofNullable(LocalTime.parse(config.getString("reset-time"), DateTimeFormatter.ISO_TIME));
        } else {
            this.resetTime = Optional.empty();
        }

        ConfigurationSection quests = config.getConfigurationSection("quests");
        if (quests != null && quests.getKeys(false) != null) {
            for (String key : quests.getKeys(false)) {
                ConfigurationSection section = quests.getConfigurationSection(key);
                if (!section.isSet("quest")) {
                    section.set("quest", key);
                }
                Optional<RDSObject> quest = RDS.createObject(RDSQuestObject.RDS_NAME, section);
                if (quest.isPresent()) {
                    quest.get().setUnique(true);
                    addEntry(quest.get());
                } else {
                    RaidCraft.LOGGER.warning("Quest " + config.getString("quest") + " was not found in " + ConfigUtil.getFileName(config));
                }
            }
        }
        if (!getContents().isEmpty()) {
            triggers.addAll(ActionAPI.createTrigger(config.getConfigurationSection("trigger")));
            rewardActions.addAll(ActionAPI.createActions(config.getConfigurationSection("actions"), Player.class));
            if (!triggers.isEmpty() && isEnabled()) {
                triggers.forEach(triggerFactory -> triggerFactory.registerListener(this));
            } else {
                RaidCraft.LOGGER.warning("Quest Pool " + ConfigUtil.getFileName(config) + " has no trigger defined and will not execute!");
            }
        } else {
            RaidCraft.LOGGER.warning("No quests in the quest pool " + getName() + " defined!");
        }
    }

    @Override
    public Class<Player> getTriggerEntityType() {

        return Player.class;
    }

    public Optional<TPlayerQuestPool> getDatabaseEntry(QuestHolder questHolder) {

        return Optional.ofNullable(RaidCraft.getDatabase(QuestPlugin.class).find(TPlayerQuestPool.class).where()
                .eq("player_id", questHolder.getId())
                .eq("quest_pool", getName())
                .findOne());
    }

    public List<TPlayerQuest> getActiveQuests(TPlayerQuestPool pool) {

        return pool.getQuests().stream()
                .filter(tPlayerQuest -> tPlayerQuest.getCompletionTime() == null
                        && tPlayerQuest.getStartTime() != null)
                .collect(Collectors.toList());
    }

    public List<QuestTemplate> getResult(QuestHolder questHolder) {

        EbeanServer database = RaidCraft.getDatabase(QuestPlugin.class);
        TPlayerQuestPool dbQuestPool;
        Optional<TPlayerQuestPool> databaseEntry = getDatabaseEntry(questHolder);
        if (databaseEntry.isPresent()) {
            dbQuestPool = databaseEntry.get();
        } else {
            dbQuestPool = new TPlayerQuestPool();
            dbQuestPool.setPlayer(database.find(TPlayer.class, questHolder.getId()));
            dbQuestPool.setQuestPool(getName());
            database.save(dbQuestPool);
        }

        List<TPlayerQuest> activeQuests = getActiveQuests(dbQuestPool);

        if (activeQuests.size() < maxActiveQuests) {
            if (getCount() > maxActiveQuests - activeQuests.size()) setCount(maxActiveQuests - activeQuests.size());

            List<String> questNames = activeQuests.stream().map(TPlayerQuest::getQuest).collect(Collectors.toList());
            List<RDSQuestObject> disabledQuests = new ArrayList<>();
            // lets disable all quests in the pool if they are active or on cooldow
            getContents().stream().filter(RDSObject::isEnabled)
                    .filter(object -> object instanceof RDSQuestObject)
                    .map(object -> (RDSQuestObject) object)
                    .filter(rdsQuestObject -> rdsQuestObject.getValue().isPresent())
                    .forEach(quest -> {
                        QuestTemplate questTemplate = quest.getValue().get();
                        if (questNames.contains(questTemplate.getId())) {
                            quest.setEnabled(false);
                            disabledQuests.add(quest);
                            return;
                        }
                        if (questHolder.hasActiveQuest(questTemplate)) {
                            quest.setEnabled(false);
                            disabledQuests.add(quest);
                            return;
                        }
                        if (questHolder.hasCompletedQuest(questTemplate)) {
                            if (!questTemplate.isRepeatable()) {
                                quest.setEnabled(false);
                                disabledQuests.add(quest);
                                return;
                            }
                            Optional<Quest> optional = questHolder.getQuest(questTemplate);
                            if (!optional.isPresent()) {
                                quest.setEnabled(false);
                                disabledQuests.add(quest);
                                return;
                            }
                            if (Instant.now().isBefore(optional.get().getCompletionTime().toInstant().plusSeconds(questTemplate.getCooldown()))) {
                                quest.setEnabled(false);
                                disabledQuests.add(quest);
                                return;
                            }
                        }
                    });

            List<QuestTemplate> result = super.loot().stream()
                    .filter(object -> object instanceof RDSQuestObject)
                    .map(object -> (RDSQuestObject) object)
                    .filter(rdsQuestObject -> rdsQuestObject.getValue().isPresent())
                    .map(rdsQuestObject -> rdsQuestObject.getValue().get())
                    .filter(object -> object.getStartRequirements().stream()
                            .allMatch(objectRequirement -> objectRequirement.test(questHolder.getPlayer())))
                    .collect(Collectors.toList());

            // ok we got our results now lets re-enable all quests again that were disable
            disabledQuests.forEach(rdsQuestObject -> rdsQuestObject.setEnabled(true));

            return result;
        }
        return new ArrayList<>();
    }

    @Override
    public boolean processTrigger(Player entity, TriggerListenerConfigWrapper trigger) {

        if (!isEnabled()) return false;

        QuestHolder questHolder = RaidCraft.getComponent(QuestManager.class).getQuestHolder(entity);
        Optional<TPlayerQuestPool> entry = getDatabaseEntry(questHolder);
        if (!entry.isPresent() || questHolder == null) {
            return false;
        }
        TPlayerQuestPool dbPool = entry.get();
        // first lets check if the cooldown has passed since the last reset
        if (Instant.now().isBefore(dbPool.getLastReset().toInstant().plusSeconds((long) getCooldown()))) {
            return false;
        }
        // check if the time now is before the defined reset time and abort
        if (getResetTime().isPresent() && LocalTime.now().isBefore(getResetTime().get())) {
            return false;
        }
        // lets see if we need to abort all active quest pool quests
        if (isAbortPrevious()) {
            List<TPlayerQuest> activeQuests = getActiveQuests(dbPool);
            for (TPlayerQuest activeQuest : activeQuests) {
                Optional<Quest> optional = questHolder.getQuest(activeQuest.getQuest());
                if (optional.isPresent() && optional.get().isActive()) {
                    optional.get().abort();
                }
            }
        }
        // ok we are good to go lets see if we can get any new quests
        List<QuestTemplate> result = getResult(questHolder);
        if (result.isEmpty()) {
            return false;
        }
        EbeanServer database = RaidCraft.getDatabase(QuestPlugin.class);
        // ok we got some quests, lets reset the timer
        int startCount = 0;
        dbPool.setLastReset(Timestamp.from(Instant.now()));
        for (QuestTemplate questTemplate : result) {
            try {
                Quest quest = questHolder.startQuest(questTemplate);
                TPlayerQuest playerQuest = database.find(TPlayerQuest.class, quest.getId());
                if (playerQuest != null) {
                    playerQuest.setQuestPool(dbPool);
                    database.update(playerQuest);
                    RaidCraft.callEvent(new QuestPoolQuestStartedEvent(quest, dbPool));
                    startCount++;
                }
            } catch (QuestException e) {
                questHolder.sendMessage(ChatColor.RED + "Unable to start QuestPool Quest: " + e.getMessage());
            }
        }
        if (startCount > 0) {
            dbPool.setLastStart(Timestamp.from(Instant.now()));
        }
        database.update(dbPool);
        return true;
    }
}
