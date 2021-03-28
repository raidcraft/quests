package de.raidcraft.quests.api.quest;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.ActionAPI;
import de.raidcraft.api.action.TriggerFactory;
import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.action.requirement.Requirement;
import de.raidcraft.api.conversations.conversation.DefaultConversation;
import de.raidcraft.quests.api.objective.ObjectiveTemplate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author Silthus
 */
@Data()
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"completionActions", "startRequirements", "objectiveTemplates", "startTrigger", "completionTrigger"})
public abstract class AbstractQuestTemplate implements QuestTemplate {

    private final String id;
    private final String name;
    private final List<String> authors = new ArrayList<>();
    private final String basePath;
    private final String friendlyName;
    private final String description;
    private final int requiredObjectiveAmount;
    private final boolean ordered;
    private final boolean locked;
    private final boolean repeatable;
    private final boolean silent;
    private final long cooldown;
    private final boolean abortable;
    private final boolean autoCompleting;
    private final Collection<Action<Player>> startActions;
    private final Collection<Action<Player>> completionActions;
    private final Collection<Requirement<Player>> startRequirements;
    private final Collection<ObjectiveTemplate> objectiveTemplates;
    private final Collection<TriggerFactory> startTrigger;
    private final Collection<TriggerFactory> activeTrigger;
    private final Collection<TriggerFactory> completionTrigger;
    // list of default conversations per phase and host - PHASE, HOST -> CONV
    private final Map<Quest.Phase, Collection<DefaultConversation>> defaultConversations;
    private final Map<Quest.Phase, Boolean> defaultConversationsClearingMap;

    public AbstractQuestTemplate(String id, ConfigurationSection data) {

        this.id = data.getString("id", id);
        String[] split = id.split("\\.");
        this.name = split[split.length - 1];
        this.basePath = id.replace("." + name, "");
        this.friendlyName = data.getString("name", name);
        this.authors.addAll(data.getStringList("authors"));
        this.description = data.getString("desc");
        this.requiredObjectiveAmount = data.getInt("required", 0);
        this.ordered = data.getBoolean("ordered", true);
        this.locked = data.getBoolean("locked", true);
        this.silent = data.getBoolean("silent", true);
        this.cooldown = data.getLong("cooldown", 0);
        this.abortable = data.getBoolean("abortable", true);
        this.autoCompleting = data.getBoolean("auto-complete", false);
        this.repeatable = cooldown > 0 || data.getBoolean("repeatable", false);
        this.startRequirements = loadRequirements(data.getConfigurationSection("requirements"));
        this.objectiveTemplates = loadObjectives(data.getConfigurationSection("objectives"));
        this.startTrigger = loadStartTrigger(data.isConfigurationSection("start-trigger") ? data.getConfigurationSection("start-trigger") : data.getConfigurationSection("trigger"));
        this.activeTrigger = ActionAPI.createTrigger(data.getConfigurationSection("active-trigger"));
        this.completionTrigger = loadCompletionTrigger(data.getConfigurationSection("complete-trigger"));
        this.startActions = ActionAPI.createActions(data.getConfigurationSection("start-actions"), Player.class);
        this.completionActions = loadActions(data.getConfigurationSection("complete-actions"));
        this.defaultConversations = loadDefaultConversations(data.getConfigurationSection("default-convs"));
        this.defaultConversationsClearingMap = loadDefaultConversationsClearingMap(data.getConfigurationSection("default-convs"));
    }

    @Override
    public String getListenerId() {

        return getId();
    }

    public void registerListeners() {

        startTrigger.forEach(trigger -> trigger.registerListener(this));
    }

    protected abstract Collection<Requirement<Player>> loadRequirements(ConfigurationSection data);

    protected abstract Collection<ObjectiveTemplate> loadObjectives(ConfigurationSection data);

    protected abstract Collection<TriggerFactory> loadStartTrigger(ConfigurationSection data);

    protected abstract Collection<TriggerFactory> loadCompletionTrigger(ConfigurationSection data);

    protected abstract Collection<Action<Player>> loadActions(ConfigurationSection data);

    protected abstract Map<Quest.Phase, Collection<DefaultConversation>> loadDefaultConversations(ConfigurationSection data);

    protected abstract Map<Quest.Phase, Boolean> loadDefaultConversationsClearingMap(ConfigurationSection section);
}
