package de.raidcraft.quests.configs;

import de.raidcraft.api.action.ActionAPI;
import de.raidcraft.api.locations.ConfiguredLocation;
import de.raidcraft.api.locations.Locations;
import de.raidcraft.quests.api.regions.AbstractQuestRegion;
import org.bukkit.configuration.ConfigurationSection;

public class ConfiguredQuestRegion extends AbstractQuestRegion {

    public ConfiguredQuestRegion(String identifier, ConfigurationSection config) {
        super(identifier);
        setName(config.getString("name", identifier));
        setDescription(config.getString("description"));
        setMinLevel(config.getInt("min-level", 1));
        setMaxLevel(config.getInt("max-level", getMinLevel()));
        Locations.fromConfig(config)
                .map(ConfiguredLocation::getLocation)
                .ifPresent(this::setStartLocation);
        setRequirements(ActionAPI.createRequirements(identifier, config.getConfigurationSection("requirements")));
    }
}
