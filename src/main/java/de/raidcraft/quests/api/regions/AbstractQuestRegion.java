package de.raidcraft.quests.api.regions;

import de.raidcraft.api.action.requirement.Requirement;
import de.raidcraft.quests.api.quest.QuestTemplate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

@Data
@EqualsAndHashCode(of = {"identifier"})
public abstract class AbstractQuestRegion implements QuestRegion {

    private final String identifier;
    private String name;
    private String description;
    private int minLevel;
    private int maxLevel;
    private Location startLocation;
    private List<Requirement<?>> requirements = new ArrayList<>();
    private final Set<QuestTemplate> quests = new HashSet<>();

    public Optional<Location> getStartLocation() {
        return Optional.ofNullable(startLocation);
    }

    @Override
    public void addQuest(QuestTemplate template) {
        this.quests.add(template);
    }
}
