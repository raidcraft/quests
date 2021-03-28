package de.raidcraft.quests.api.objective;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.trigger.TriggerListenerConfigWrapper;
import de.raidcraft.quests.api.events.ObjectiveCompleteEvent;
import de.raidcraft.quests.api.events.ObjectiveStartedEvent;
import de.raidcraft.quests.api.events.TaskCompletedEvent;
import de.raidcraft.quests.api.holder.QuestHolder;
import de.raidcraft.quests.api.quest.Quest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Silthus
 */
@Data
@EqualsAndHashCode(of = {"id", "quest", "objectiveTemplate"})
public abstract class AbstractPlayerObjective implements PlayerObjective {

    private final int id;
    private final Quest quest;
    private final ObjectiveTemplate objectiveTemplate;
    private final List<PlayerTask> tasks;
    private boolean active = false;
    private Timestamp completionTime;
    private Timestamp abortionTime;
    private Timestamp startTime;

    public AbstractPlayerObjective(int id, Quest quest, ObjectiveTemplate objectiveTemplate) {

        this.id = id;
        this.quest = quest;
        this.objectiveTemplate = objectiveTemplate;
        this.tasks = loadTasks();
    }

    protected abstract List<PlayerTask> loadTasks();

    @Override
    public String getListenerId() {
        return getQuest().getListenerId() + "." + getId();
    }


    @Override
    public Optional<Player> getEntity() {
        return Optional.ofNullable(getQuest().getPlayer());
    }

    @Override
    public Optional<PlayerTask> getTask(int id) {
        return getTasks().stream()
                .filter(task -> task.getId() == id)
                .findAny();
    }

    @Override
    public boolean processTrigger(Player player, TriggerListenerConfigWrapper trigger) {
        autoComplete(player);
        return true;
    }

    private void autoComplete(Player player) {
        if (hasCompletedAllTasks() && getObjectiveTemplate().getRequirements().stream()
                .allMatch(requirement -> requirement.test(player))) {
            if (getObjectiveTemplate().isAutoCompleting()) {
                complete();
            }
        }
    }

    public void updateListeners() {

        if (!isCompleted() && !isAborted()) {
            if (!isActive()) {
                if (!isStarted()) {
                    // execute our objective start actions
                    setActive(true);
                    getObjectiveTemplate().getStartActions().forEach(action -> action.accept(getQuestHolder().getPlayer()));
                    setStartTime(Timestamp.from(Instant.now()));
                    ObjectiveStartedEvent event = new ObjectiveStartedEvent(this);
                    RaidCraft.callEvent(event);
                    save();
                }
                // register our start trigger
                updateDefaultConversations();
                getObjectiveTemplate().getTrigger().forEach(factory -> factory.registerListener(this));
                getUncompletedTasks().forEach(PlayerTask::updateListeners);
                setActive(true);
            }
        } else {
            unregisterListeners();
            updateDefaultConversations();
        }
    }

    public void unregisterListeners() {

        getObjectiveTemplate().getTrigger().forEach(factory -> factory.unregisterListener(this));
        unregisterTaskListeners();
        setActive(false);
    }

    private void updateTaskListeners() {
        if (isCompleted()) {
            unregisterListeners();
            return;
        }
        if (!isActive()) {
            // do not register task listeners if the objective is not started
            return;
        }
        if (hasCompletedAllTasks()) {
            unregisterTaskListeners();
            this.updateDefaultConversations();
            if (getObjectiveTemplate().getTrigger().isEmpty()) {
                this.autoComplete(getQuest().getPlayer());
            } else {
                updateListeners();
            }
            return;
        }

        for (PlayerTask task : getTasks()) {
            if (!task.isCompleted()) {
                // lets register the listeners of our task
                task.updateListeners();
            } else {
                task.unregisterListeners();
            }
        }
    }

    private boolean hasCompletedAllTasks() {
        if (getTasks().isEmpty()) return true;
        List<PlayerTask> uncompletedTasks = getUncompletedTasks();
        boolean completed = uncompletedTasks.isEmpty()
                || (getObjectiveTemplate().getRequiredTaskCount() > 0
                && getObjectiveTemplate().getRequiredTaskCount() <= uncompletedTasks.size());
        if (!uncompletedTasks.isEmpty() && !completed) {
            int optionalObjectives = 0;
            for (PlayerTask task : uncompletedTasks) {
                if (task.getTaskTemplate().isOptional()) optionalObjectives++;
            }
            if (optionalObjectives == uncompletedTasks.size()) {
                completed = true;
            }
        }
        return completed;
    }

    private List<PlayerTask> getUncompletedTasks() {
        return getTasks().stream().filter(task -> !task.isCompleted()).collect(Collectors.toList());
    }

    private void unregisterTaskListeners() {
        getTasks().forEach(PlayerTask::unregisterListeners);
    }

    @Override
    public QuestHolder getQuestHolder() {

        return quest.getHolder();
    }

    @Override
    public Quest.Phase getPhase() {
        if (isAborted()) return Quest.Phase.ABORTED;
        if (isCompleted()) return Quest.Phase.COMPLETED;
        if (hasCompletedAllTasks()) return Quest.Phase.OJECTIVES_COMPLETED;
        if (isActive()) return Quest.Phase.ACTIVE;
        if (isStarted()) return Quest.Phase.ACTIVE;
        return Quest.Phase.NOT_STARTED;
    }

    @Override
    public boolean isCompleted() {

        return completionTime != null;
    }

    @Override
    public boolean isAborted() {

        return abortionTime != null;
    }

    @Override
    public boolean isHidden() {

        if (!getObjectiveTemplate().isHidden() && !getObjectiveTemplate().isSecret()) return false;
        if (getObjectiveTemplate().isHidden()) {
            return !isActive() && !isCompleted();
        }
        return getObjectiveTemplate().isSecret();
    }

    @Override
    public boolean isStarted() {
        return startTime != null;
    }

    @Override
    public void onTaskComplete(PlayerTask task) {
        save();
        TaskCompletedEvent event = new TaskCompletedEvent(task);
        RaidCraft.callEvent(event);
        updateTaskListeners();
    }

    @Override
    public void complete() {

        if (isCompleted()) return;
        ObjectiveCompleteEvent event = new ObjectiveCompleteEvent(this);
        RaidCraft.callEvent(event);
        if (event.isCancelled()) return;
        unregisterListeners();
        this.completionTime = new Timestamp(System.currentTimeMillis());
        save();
        // set our default conversations
        this.updateDefaultConversations();
        // lets execute all objective actions
        getObjectiveTemplate().getActions().forEach(action -> action.accept(getQuestHolder().getPlayer()));
        getQuest().onObjectCompletion(this);
    }

    protected void updateDefaultConversations() {
        if (getObjectiveTemplate().getDefaultConversationsClearingMap().get(getPhase())) {
            for (Quest.Phase phase : Quest.Phase.values()) {
                getObjectiveTemplate().getDefaultConversations().get(phase)
                        .forEach(defaultConversation -> defaultConversation.unsetConversation(getQuestHolder().getPlayerId()));
            }
        }
        getObjectiveTemplate().getDefaultConversations().get(getPhase())
                .forEach(defaultConversation -> defaultConversation.setConversation(getQuestHolder().getPlayerId()));
    }

    @Override
    public void abort() {

        unregisterListeners();
        setCompletionTime(null);
        setAbortionTime(Timestamp.from(Instant.now()));
        save();
    }

    @Override
    public int compareTo(@NonNull PlayerObjective o) {

        return this.getObjectiveTemplate().compareTo(o.getObjectiveTemplate());
    }
}
