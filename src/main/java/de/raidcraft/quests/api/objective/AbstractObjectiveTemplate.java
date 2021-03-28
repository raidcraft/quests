package de.raidcraft.quests.api.objective;

import de.raidcraft.api.action.TriggerFactory;
import de.raidcraft.api.action.action.Action;
import de.raidcraft.api.action.requirement.Requirement;
import de.raidcraft.api.conversations.conversation.DefaultConversation;
import de.raidcraft.quests.api.quest.Quest;
import de.raidcraft.quests.api.quest.QuestTemplate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;

/**
 * @author Silthus
 */
@Data
@EqualsAndHashCode(of = {"id", "questTemplate"})
@ToString(exclude = {"requirements", "trigger", "actions", "tasks"})
public abstract class AbstractObjectiveTemplate implements ObjectiveTemplate {

    private final int id;
    private final String friendlyName;
    private final String description;
    private final boolean optional;
    private final boolean hidden;
    private final boolean secret;
    private final boolean autoCompleting;
    private final boolean silent;
    private final int requiredTaskCount;
    private final QuestTemplate questTemplate;
    private final Collection<Requirement<Player>> requirements;
    private final Collection<TriggerFactory> trigger;
    private final Collection<Action<Player>> actions;
    private final Collection<Action<Player>> startActions;
    private final Collection<TaskTemplate> tasks;
    private final Map<Quest.Phase, Collection<DefaultConversation>> defaultConversations;
    private final Map<Quest.Phase, Boolean> defaultConversationsClearingMap;

    public AbstractObjectiveTemplate(int id, QuestTemplate questTemplate, ConfigurationSection data) {

        this.id = id;
        this.friendlyName = data.getString("name");
        this.description = data.getString("desc");
        this.optional = data.getBoolean("optional", false);
        this.hidden = data.getBoolean("hidden", false);
        this.secret = data.getBoolean("secret", false);
        this.silent = data.getBoolean("silent", false);
        this.requiredTaskCount = data.getInt("required-tasks", 0);
        this.autoCompleting = data.getBoolean("auto-complete", true);
        this.questTemplate = questTemplate;
        this.requirements = loadRequirements(data.getConfigurationSection("requirements"));
        this.trigger = loadTrigger(data.getConfigurationSection("trigger"));
        this.startActions = loadStartActions(data.getConfigurationSection("start-actions"));
        this.actions = loadActions(data.getConfigurationSection("complete-actions"));
        this.tasks = loadTasks(data.getConfigurationSection("tasks"));
        this.defaultConversations = loadDefaultConversations(data.getConfigurationSection("default-convs"));
        this.defaultConversationsClearingMap = loadDefaultConversationsClearingMap(data.getConfigurationSection("default-convs"));
    }

    protected abstract Collection<Requirement<Player>> loadRequirements(ConfigurationSection data);

    protected abstract Collection<TriggerFactory> loadTrigger(ConfigurationSection data);

    protected abstract Collection<Action<Player>> loadActions(ConfigurationSection data);

    protected abstract Collection<Action<Player>> loadStartActions(ConfigurationSection data);

    protected abstract Collection<TaskTemplate> loadTasks(ConfigurationSection data);

    protected abstract Map<Quest.Phase, Collection<DefaultConversation>> loadDefaultConversations(ConfigurationSection data);

    protected abstract Map<Quest.Phase, Boolean> loadDefaultConversationsClearingMap(ConfigurationSection data);

    @Override
    public int compareTo(@NonNull ObjectiveTemplate o) {

        if (getId() < o.getId()) {
            return -1;
        }
        if (getId() > o.getId()) {
            return 1;
        }
        return 0;
    }
}
