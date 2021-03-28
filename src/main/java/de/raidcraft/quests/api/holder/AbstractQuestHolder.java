package de.raidcraft.quests.api.holder;

import de.raidcraft.api.quests.QuestException;
import de.raidcraft.quests.api.objective.PlayerObjective;
import de.raidcraft.quests.api.quest.Quest;
import de.raidcraft.quests.api.quest.QuestTemplate;
import de.raidcraft.quests.ui.QuestInventory;
import de.raidcraft.util.CaseInsensitiveMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Silthus
 */
@Data
@ToString(exclude = "activeQuests")
@EqualsAndHashCode(of = "id")
public abstract class AbstractQuestHolder implements QuestHolder {

    private final int id;
    private final UUID player;
    private final Map<String, Quest> activeQuests = new CaseInsensitiveMap<>();
    private final QuestInventory questInventory;

    public AbstractQuestHolder(int id, UUID player) {

        this.id = id;
        this.player = player;
        this.questInventory = new QuestInventory(this);
    }

    @Override
    public UUID getPlayerId() {

        return getPlayer() == null ? player : getPlayer().getUniqueId();
    }

    @Override
    public Player getPlayer() {

        return Bukkit.getPlayer(player);
    }

    @Override
    public boolean hasQuest(String quest) {

        return activeQuests.containsKey(quest)
                || getAllQuests().stream().anyMatch(q -> q.getFullName().equals(quest));
    }

    @Override
    public boolean hasActiveQuest(String name) {

        return getQuest(name).map(Quest::isActive).orElse(false);
    }

    @Override
    public boolean hasCompletedQuest(String questId) {
        return getCompletedQuests().stream()
                .map(Quest::getTemplate)
                .map(QuestTemplate::getId)
                .anyMatch(id -> id.equalsIgnoreCase(questId));
    }

    @Override
    public Optional<Quest> getQuest(QuestTemplate questTemplate) {

        String id = questTemplate.getId();
        if (activeQuests.containsKey(id)) {
            return Optional.of(activeQuests.get(id));
        }
        return getAllQuests().stream().filter(q -> q.getTemplate().equals(questTemplate)).findFirst();
    }

    @Override
    public Optional<Quest> getQuest(String name) {

        if (activeQuests.containsKey(name)) {
            return Optional.of(activeQuests.get(name));
        }
        List<Quest> foundQuests = getAllQuests().stream()
                .filter(quest -> quest.getFullName().equalsIgnoreCase(name) || quest.getFullName().startsWith(name)).collect(Collectors.toList());
        if (foundQuests.isEmpty()) {
            return Optional.empty();
        }
        if (foundQuests.size() > 1) {
            // we have repeatable quests lets get the last quest or active quest
            Optional<Quest> first = foundQuests.stream().filter(Quest::isActive).findFirst();
            if (first.isPresent()) return first;
            // now we only have completed quest
            foundQuests.sort(Comparator.comparing(Quest::getCompletionTime));
            return Optional.of(foundQuests.get(foundQuests.size() - 1));
        }
        return Optional.of(foundQuests.get(0));
    }

    @Override
    public List<Quest> getCompletedQuests() {

        return getAllQuests().stream()
                .filter(Quest::isCompleted)
                .collect(Collectors.toList());
    }

    @Override
    public List<Quest> getActiveQuests() {

        return new ArrayList<>(activeQuests.values());
    }

    @Override
    public Quest startQuest(QuestTemplate template) throws QuestException {

        if (template.isLocked() && !getPlayer().hasPermission("rcquests.quest.start-locked")) {
            throw new QuestException("Diese Quest ist aktuell gesperrt und kann nicht angenommen werden.");
        }
        return null;
    }

    @Override
    public void addQuest(Quest quest) {

        if (quest.isActive()) {
            activeQuests.put(quest.getFullName(), quest);
        }
    }

    @Override
    public void removeQuest(Quest quest) {

        activeQuests.remove(quest.getFullName());
        unregister(quest);
    }

    @Override
    public void unregister() {
        getAllQuests().forEach(this::unregister);
    }

    private void unregister(Quest quest) {
        quest.getObjectives().forEach(PlayerObjective::unregisterListeners);
    }
}