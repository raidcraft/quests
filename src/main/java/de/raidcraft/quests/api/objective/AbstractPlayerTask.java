package de.raidcraft.quests.api.objective;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.trigger.TriggerListenerConfigWrapper;
import de.raidcraft.quests.api.events.TaskCompleteEvent;
import de.raidcraft.quests.api.events.TaskStartedEvent;
import de.raidcraft.quests.api.holder.QuestHolder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Data
@EqualsAndHashCode(of = {"id", "objective", "taskTemplate"})
public abstract class AbstractPlayerTask implements PlayerTask {

    private final int id;
    private final PlayerObjective objective;
    private final TaskTemplate taskTemplate;
    private boolean active = false;
    private Timestamp completionTime;
    private Timestamp abortionTime;

    @Override
    public String getListenerId() {
        return getObjective().getListenerId() + "." + getId();
    }

    @Override
    public Optional<Player> getEntity() {
        return Optional.ofNullable(getQuestHolder().getPlayer());
    }

    @Override
    public boolean processTrigger(Player player, TriggerListenerConfigWrapper trigger) {

        if (getTaskTemplate().getRequirements().stream()
                .allMatch(requirement -> requirement.test(player))) {
            if (getTaskTemplate().isAutoCompleting()) {
                complete();
            }
        }
        return true;
    }

    public void updateListeners() {

        if (!isCompleted() && !isAborted()) {
            if (!isActive()) {
                // register our start trigger
                getTaskTemplate().getTrigger().forEach(factory -> factory.registerListener(this));
                setActive(true);
                TaskStartedEvent event = new TaskStartedEvent(this);
                RaidCraft.callEvent(event);
            }
        } else {
            unregisterListeners();
        }
    }

    public void unregisterListeners() {

        getTaskTemplate().getTrigger().forEach(factory -> factory.unregisterListener(this));
        setActive(false);
    }

    @Override
    public QuestHolder getQuestHolder() {

        return getQuest().getHolder();
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

        if (!getTaskTemplate().isHidden() && !getTaskTemplate().isSecret()) return false;
        if (getTaskTemplate().isHidden()) {
            return !isActive() && !isCompleted();
        }
        return getTaskTemplate().isSecret();
    }

    @Override
    public void complete() {

        if (isCompleted()) return;
        TaskCompleteEvent event = new TaskCompleteEvent(this);
        RaidCraft.callEvent(event);
        if (event.isCancelled()) return;
        unregisterListeners();
        this.completionTime = new Timestamp(System.currentTimeMillis());
        save();
        // lets execute all objective actions
        getTaskTemplate().getActions().forEach(action -> action.accept(getQuestHolder().getPlayer()));
        getObjective().onTaskComplete(this);
    }

    @Override
    public void abort() {

        unregisterListeners();
        setCompletionTime(null);
        setAbortionTime(Timestamp.from(Instant.now()));
        save();
    }

    @Override
    public int compareTo(@NonNull PlayerTask o) {

        return this.getTaskTemplate().compareTo(o.getTaskTemplate());
    }
}
