package de.raidcraft.quests;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.Component;
import de.raidcraft.api.config.ConfigLoader;
import de.raidcraft.api.config.ConfigurationBase;
import de.raidcraft.api.config.SimpleConfiguration;
import de.raidcraft.api.player.UnknownPlayerException;
import de.raidcraft.api.quests.QuestException;
import de.raidcraft.api.quests.QuestProvider;
import de.raidcraft.api.quests.RCQuestConfigsLoaded;
import de.raidcraft.quests.api.holder.QuestHolder;
import de.raidcraft.quests.api.objective.PlayerObjective;
import de.raidcraft.quests.api.quest.Quest;
import de.raidcraft.quests.api.quest.QuestTemplate;
import de.raidcraft.quests.api.regions.QuestRegion;
import de.raidcraft.quests.configs.ConfiguredQuestRegion;
import de.raidcraft.quests.tables.TPlayer;
import de.raidcraft.quests.tables.TPlayerQuest;
import de.raidcraft.quests.ui.QuestUI;
import de.raidcraft.util.CaseInsensitiveMap;
import de.raidcraft.util.ConfigUtil;
import io.ebean.EbeanServer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Silthus
 */
public final class QuestManager implements QuestProvider, Component {

    private final QuestPlugin plugin;
    private final Map<String, ConfigLoader> configLoader = new CaseInsensitiveMap<>();
    private final Map<String, QuestTemplate> loadedQuests = new CaseInsensitiveMap<>();
    private final Map<String, QuestPool> loadedQuestPools = new CaseInsensitiveMap<>();
    private final Map<String, QuestRegion> questRegions = new CaseInsensitiveMap<>();
    private final Map<ConfigLoader, Map<String, ConfigurationBase<QuestPlugin>>> queuedConfigLoaders = new HashMap<>();

    private final Map<UUID, QuestHolder> questPlayers = new HashMap<>();

    private boolean loadedQuestFiles = false;
    private int notLoadedQuests = 0;

    protected QuestManager(final QuestPlugin plugin) {
        this.plugin = plugin;
        RaidCraft.registerComponent(QuestManager.class, this);
        // lets register our own config loader with a high priority to load it last
        registerQuestConfigLoader(new ConfigLoader<QuestPlugin>(plugin, "quest", 100) {
            @Override
            public void loadConfig(String id, ConfigurationBase<QuestPlugin> config) {
                registerQuest(id, config);
            }
        });
        registerQuestConfigLoader(new ConfigLoader<QuestPlugin>(plugin, "pool", 1000) {
            @Override
            public void loadConfig(String id, ConfigurationBase<QuestPlugin> config) {

                QuestPool pool = new QuestPool(id, config);
                loadedQuestPools.put(id, pool);
                plugin.getLogger().info("Loaded quest pool: " + id + " - " + pool.getFriendlyName());
            }
        });
        registerQuestConfigLoader(new ConfigLoader<QuestPlugin>(plugin, "region") {
            @Override
            public void loadConfig(String id, ConfigurationBase<QuestPlugin> config) {
                registerQuestRegion(id, config);
            }
        });
    }

    public void load() {
        plugin.getLogger().info("Loading quest configurations...");
        // we need to look recursivly thru all folders under the defined base folder
        File baseFolder = new File(plugin.getDataFolder(), plugin.getConfiguration().quests_base_folder);
        baseFolder.mkdirs();
        loadQuestConfigs(baseFolder, "");
        plugin.getLogger().info("... queued " + queuedConfigLoaders.values().size() + " quest configs ...");
        // lets sort our loaders by priority
        queuedConfigLoaders.keySet().stream()
                .sorted()
                .forEachOrdered(loader -> queuedConfigLoaders.get(loader)
                        .forEach(loader::loadConfig));

        queuedConfigLoaders.keySet().forEach(ConfigLoader::onLoadingComplete);

        plugin.getLogger().info("... loaded " + loadedQuests.size() + "/" + (loadedQuests.size() + notLoadedQuests) + " quests");
        loadedQuestFiles = true;
        RaidCraft.callEvent(new RCQuestConfigsLoaded());
    }

    private void loadQuestConfigs(File baseFolder, String path) {

        for (File file : baseFolder.listFiles()) {
            String fileName = file.getName();
            if (file.isDirectory()) {
                loadQuestConfigs(file, path + "." + fileName.toLowerCase());
            } else {
                path = StringUtils.strip(path, ".");
                for (ConfigLoader loader : configLoader.values()) {
                    if (file.getName().toLowerCase().endsWith(loader.getSuffix())) {
                        String id = (path + "." + file.getName().toLowerCase()).replace(loader.getSuffix(), "");
                        id = StringUtils.strip(id, ".");
                        ConfigurationBase<QuestPlugin> configFile = plugin.configure(new SimpleConfiguration<>(plugin, file));
                        // repace "this." with the absolte path, feature: relative path
                        configFile = ConfigUtil.replacePathReferences(configFile, path);
                        if (!queuedConfigLoaders.containsKey(loader)) {
                            queuedConfigLoaders.put(loader, new HashMap<>());
                        }
                        queuedConfigLoaders.get(loader).put(id, configFile);
                    }
                }
            }
        }
    }

    public void unload() {

        queuedConfigLoaders.keySet().stream()
                .sorted()
                .forEachOrdered(loader -> queuedConfigLoaders.get(loader)
                        .forEach((id, configurationSection) -> loader.unloadConfig(id)));
        queuedConfigLoaders.clear();

        loadedQuestPools.values()
                .forEach(questPool -> questPool.getTriggers()
                        .forEach(triggerFactory -> triggerFactory.unregisterListener(questPool)));
        loadedQuestPools.clear();
        questPlayers.values().forEach(holder -> {
            holder.save();
            holder.getAllQuests()
                    .forEach(quest -> quest.getObjectives()
                            .forEach(PlayerObjective::unregisterListeners));
        });
        loadedQuests.values()
                .forEach(template -> template.getStartTrigger()
                        .forEach(trigger -> trigger.unregisterListener(template)));
        loadedQuests.clear();
        questPlayers.clear();
        loadedQuestFiles = false;
    }

    public void reload() {
        unload();
        load();
    }

    @Override
    public void registerQuestConfigLoader(ConfigLoader loader) {
        if (configLoader.containsKey(loader.getSuffix())) {
            RaidCraft.LOGGER.warning("Config loader with the suffix " + loader.getSuffix() + " is already registered!");
            return;
        }
        configLoader.put(loader.getSuffix(), loader);
        if (loadedQuestFiles) {
            load();
        }
    }

    @Nullable
    @Override
    public ConfigLoader getQuestConfigLoader(String suffix) {

        ConfigLoader loader = null;
        if (configLoader.containsKey(suffix)) {
            loader = configLoader.get(suffix);
        } else if (configLoader.containsKey("." + suffix + ".yml")) {
            loader = configLoader.get("." + suffix + ".yml");
        }
        return loader;
    }

    @Override
    public boolean hasQuestItem(Player player, ItemStack item, int amount) {

        QuestHolder questHolder = getQuestHolder(player);
        if (questHolder == null) {
            return false;
        }
        return questHolder.getQuestInventory().contains(item, amount);
    }

    @Override
    public void removeQuestItem(Player player, ItemStack... itemStack) {

        QuestHolder questHolder = getQuestHolder(player);
        if (questHolder == null) return;
        questHolder.getQuestInventory().removeItem(itemStack);
    }

    @Override
    public void addQuestItem(Player player, ItemStack... itemStack) {

        QuestHolder questHolder = getQuestHolder(player);
        if (questHolder == null) return;
        questHolder.getQuestInventory().addItem(itemStack);
    }

    public void registerQuest(String identifier, ConfigurationSection config) {
        if (config.isSet("worlds") && config.isList("worlds")) {
            List<String> worlds = config.getStringList("worlds").stream().map(String::toLowerCase).collect(Collectors.toList());
            Optional<World> any = Bukkit.getServer().getWorlds().stream()
                    .filter(w -> worlds.contains(w.getName().toLowerCase()))
                    .findAny();
            if (!any.isPresent()) {
                notLoadedQuests++;
                if (plugin.getConfiguration().debugQuestLoading) {
                    plugin.getLogger().info("Excluded Quest " + identifier + " because the required world (" + config.get("worlds") + ") is not loaded ." +
                            "The following worlds are loaded: " + Bukkit.getServer().getWorlds().stream().map(World::getName).collect(Collectors.toList()));
                }
                return;
            }
        }
        SimpleQuestTemplate quest = new SimpleQuestTemplate(identifier, config);
        // lets register the triggers of the quest
        quest.registerListeners();
        loadedQuests.put(identifier, quest);
        if (questRegions.containsKey(config.getString("region"))) {
            questRegions.get(config.getString("region")).addQuest(quest);
        }
        plugin.getLogger().info("Loaded quest: " + identifier + " - " + quest.getFriendlyName());
    }

    public QuestRegion registerQuestRegion(String identifier, ConfigurationSection config) {
        if (questRegions.containsKey(identifier)) {
            plugin.getLogger().warning("Cannot register duplicate quest region " + identifier + ": " + ConfigUtil.getFileName(config));
            return questRegions.get(identifier);
        }
        ConfiguredQuestRegion region = new ConfiguredQuestRegion(identifier, config);
        questRegions.put(identifier, region);
        plugin.getLogger().info("Registered quest region: " + identifier);
        return region;
    }

    public QuestHolder clearPlayerCache(UUID playerId) {
        return questPlayers.remove(playerId);
    }

    public void purgePlayerHistory(UUID playerId, @Nullable QuestRegion region, boolean teleport) throws UnknownPlayerException {
        QuestHolder player = getPlayer(playerId);
        for (Quest quest : player.getAllQuests()) {
            if (region != null) {
                if (region.getQuests().contains(quest.getTemplate())) quest.delete();
            } else {
                quest.delete();
            }
        }
        player.getQuestInventory().clear();
        clearPlayerCache(playerId);

        Player onlinePlayer = Bukkit.getPlayer(playerId);
        if (region != null && teleport && onlinePlayer != null) {
            region.getStartLocation().ifPresent(onlinePlayer::teleport);
        }
    }

    public Optional<QuestRegion> getQuestRegion(String identifier) {
        return Optional.ofNullable(questRegions.get(identifier));
    }

    public Collection<QuestRegion> getQuestRegions() {
        return questRegions.values();
    }

    public QuestHolder getQuestHolder(Player player) {
        try {
            return getPlayer(player.getUniqueId());
        } catch (UnknownPlayerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public QuestHolder getPlayer(UUID playerId) throws UnknownPlayerException {
        Player bukkitPlayer = Bukkit.getPlayer(playerId);
        if (bukkitPlayer == null) {
            throw new UnknownPlayerException("Player not online?");
        }
        String name = bukkitPlayer.getName();
        if (!questPlayers.containsKey(playerId)) {
            TPlayer table = plugin.getRcDatabase().find(TPlayer.class).where()
                    .eq("player_id", playerId).findOne();
            if (table == null) {
                table = new TPlayer();
                table.setPlayer(name.toLowerCase());
                table.setPlayerId(playerId);
                plugin.getRcDatabase().save(table);
            }
            questPlayers.put(playerId, new BukkitQuestHolder(table.getId(), playerId));
        }
        return questPlayers.get(playerId);
    }

    public QuestTemplate getQuestTemplate(String name) throws QuestException {

        String questName = name.toLowerCase().replace(".quest", "").trim();

        Optional<QuestTemplate> questTemplate = getQuestTemplateById(questName);
        if (questTemplate.isPresent()) return questTemplate.get();

        List<QuestTemplate> foundQuests = loadedQuests.values().stream()
                .filter(quest -> quest.getFriendlyName().toLowerCase().contains(questName))
                .collect(Collectors.toList());

        if (foundQuests.isEmpty()) {
            throw new QuestException("Du hast keine Quest mit dem Namen: " + questName);
        }
        if (foundQuests.size() > 1) {
            throw new QuestException("Du hast mehrere Quests mit dem Namen " + questName + ": " + StringUtils.join(foundQuests, ", "));
        }
        return foundQuests.get(0);
    }

    public Optional<QuestTemplate> getQuestTemplateById(String id) {

        Validate.notNull(id);
        String key = id.toLowerCase().trim();

        if (loadedQuests.containsKey(key)) return Optional.ofNullable(loadedQuests.get(key));

        return Optional.ofNullable(loadedQuests.keySet().stream()
                .filter(questId -> questId.startsWith(key))
                .map(loadedQuests::get)
                .collect(toSingleton()));
    }

    /**
     * Will create a new {@link Quest} for the given {@link QuestHolder}.
     * If an existing active Quest is found an exception will be thrown.
     * If a completed quest is found and the quest is not repeatable an exception will be thrown.
     * <p/>
     * Creating this quest does not mean the quest will be started! {@link Quest#start()} needs to be called first.
     *
     * @param holder   to create quest for
     * @param template to create quest of
     *
     * @return new quest if no active quest is found. If the quest is repeatable the old quest must be completed
     *
     * @throws QuestException if quest is active or not repeatable and completed
     */
    public Quest createQuest(QuestHolder holder, QuestTemplate template) throws QuestException {

        Optional<Quest> optionalQuest = holder.getQuest(template);
        if (optionalQuest.isPresent()) {
            if (optionalQuest.get().isActive()) {
                throw new QuestException("Quest is already active and cannot be created! "
                        + template.getFriendlyName() + " for " + holder.getPlayer().getName());
            }
            if (optionalQuest.get().isCompleted() && !template.isRepeatable()) {
                throw new QuestException("Quest " + template.getFriendlyName() + " cannot be repeated and is already completed!");
            }
            throw new QuestException("Quest cannot be created because it is already present!");
        }

        EbeanServer database = plugin.getRcDatabase();
        TPlayerQuest playerQuest = new TPlayerQuest();
        playerQuest.setPlayer(database.find(TPlayer.class, holder.getId()));
        playerQuest.setQuest(template.getId());
        database.save(playerQuest);

        return new SimpleQuest(playerQuest, template, holder);
    }

    public List<Quest> getAllQuests(QuestHolder holder) {

        List<Quest> quests = new ArrayList<>();
        EbeanServer database = RaidCraft.getDatabase(QuestPlugin.class);
        List<TPlayerQuest> databaseEntries = database.find(TPlayerQuest.class).where().eq("player_id", holder.getId()).findList();
        for (TPlayerQuest quest : databaseEntries) {
            try {
                QuestTemplate questTemplate = getQuestTemplate(quest.getQuest());
                SimpleQuest simpleQuest = new SimpleQuest(quest, questTemplate, holder);
                quests.add(simpleQuest);
            } catch (QuestException e) {
                RaidCraft.LOGGER.warning(e.getMessage());
            }
        }
        return quests;
    }

    public List<QuestPool> getQuestPools() {

        return new ArrayList<>(loadedQuestPools.values());
    }

    /**
     * Tries to find the given quest using the following search order:
     * * match of the full unique path to the quest (e.g. world.foo.bar.quest-displayName)
     * * match of the unique displayName of the quest (e.g. quest-displayName)
     * * full match of the friendly displayName in variants by replacing space with _ and - (e.g. Quest Name -> "Quest-Name" will match)
     * * partial match of the quest displayName with contains (e.g. Quest Name -> "Quest" will match)
     *
     * @param holder of the quest
     * @param name   to match for
     *
     * @return matching quest
     *
     * @throws QuestException is thrown if no or more than one quest matches
     */
    public Quest findQuest(QuestHolder holder, String name) throws QuestException {

        List<Quest> allQuests = getAllQuests(holder);
        // match of the full unique path to the quest (e.g. world.foo.bar.quest-displayName)
        Optional<Quest> first = allQuests.stream().filter(quest -> quest.getFullName().equalsIgnoreCase(name)).findFirst();
        if (first.isPresent()) return first.get();
        // match of the unique displayName of the quest (e.g. quest-displayName)
        first = allQuests.stream().filter(quest -> quest.getName().equalsIgnoreCase(name)).findFirst();
        if (first.isPresent()) return first.get();
        // full match of the friendly displayName in variants by replacing space with _ and -
        // (e.g. Quest Name -> "Quest-Name" will match)
        List<Quest> quests = allQuests.stream().filter(quest -> quest.getFriendlyName().equalsIgnoreCase(name)
                        || quest.getFriendlyName().equalsIgnoreCase(name.replace("-", " "))
                        || quest.getFriendlyName().equalsIgnoreCase(name.replace("_", " "))
        ).collect(Collectors.toList());
        if (quests.size() > 1) {
            throw new QuestException("Multiple Quests with the displayName " + name + " found: " +
                    StringUtils.join(quests.stream().map(Quest::getFriendlyName).collect(Collectors.toList()), ","));
        }
        if (quests.size() == 1) return quests.get(0);
        // partial match of the quest displayName with contains (e.g. Quest Name -> "Quest" will match)
        quests = allQuests.stream().filter(quest ->
                quest.getFullName().endsWith(name)
                        || quest.getFriendlyName().contains(name))
                .collect(Collectors.toList());
        if (quests.size() > 1) {
            throw new QuestException("Multiple Quests with the displayName " + name + " found: " +
                    StringUtils.join(quests.stream().map(Quest::getFriendlyName).collect(Collectors.toList()), ","));
        }
        if (quests.size() == 1) return quests.get(0);
        throw new QuestException("No matching Quest with the displayName " + name + " found!");
    }

    public Optional<QuestPool> getQuestPool(String identifier) {

        return Optional.ofNullable(loadedQuestPools.get(identifier));
    }

    public void clearCache(Player player) {

        questPlayers.remove(player.getUniqueId());
    }

    public void openQuestLog(Player player) {
        QuestHolder questHolder = getQuestHolder(player);
        new QuestUI(questHolder, questHolder.getActiveQuests(), QuestUI.Type.ACTIVE).open();
    }

    public static <T> Collector<T, ?, T> toSingleton() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        return null;
                    }
                    return list.get(0);
                }
        );
    }
}
