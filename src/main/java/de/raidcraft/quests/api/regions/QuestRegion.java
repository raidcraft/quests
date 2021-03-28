package de.raidcraft.quests.api.regions;

import de.raidcraft.api.action.requirement.RequirementHolder;
import de.raidcraft.quests.api.quest.QuestTemplate;
import org.bukkit.Location;

import java.util.Collection;
import java.util.Optional;

public interface QuestRegion extends RequirementHolder {

    String getIdentifier();

    String getName();

    String getDescription();

    int getMinLevel();

    int getMaxLevel();

    Optional<Location> getStartLocation();

    void addQuest(QuestTemplate template);

    Collection<QuestTemplate> getQuests();
}
