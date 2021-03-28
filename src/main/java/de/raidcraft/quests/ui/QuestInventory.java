package de.raidcraft.quests.ui;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomSkullType;
import de.raidcraft.api.items.Skull;
import de.raidcraft.api.storage.ItemStorage;
import de.raidcraft.api.storage.StorageException;
import de.raidcraft.quests.QuestPlugin;
import de.raidcraft.quests.api.holder.QuestHolder;
import de.raidcraft.quests.tables.TQuestItem;
import io.ebean.EbeanServer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * @author mdoering
 */
public class QuestInventory implements Listener {

    private static final int LEFT_BUTTON = 45;
    private static final int RIGHT_BUTTON = 53;
    private final ItemStack leftButton;
    private final ItemStack rightButton;
    private final ItemStack filler;
    private final QuestHolder holder;
    private final ItemStorage storage;
    private final List<Inventory> inventories = new ArrayList<>();
    private int currentInventory = 0;

    public QuestInventory(QuestHolder holder) {

        RaidCraft.getComponent(QuestPlugin.class).registerEvents(this);
        this.holder = holder;
        this.storage = new ItemStorage("quest_inventory");
        // left button
        this.leftButton = Skull.getSkull(CustomSkullType.ARROW_LEFT);
        this.rightButton = Skull.getSkull(CustomSkullType.ARROW_RIGHT);
        // filler item
        this.filler = new ItemStack(Material.PUMPKIN_STEM);
        ItemMeta fillerMetaData = this.filler.getItemMeta();
        fillerMetaData.setDisplayName("");
        this.filler.setItemMeta(fillerMetaData);
        load();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {

        if (!event.getWhoClicked().equals(holder.getPlayer()) || !inventories.contains(event.getInventory())) {
            return;
        }
        int inventoryIndex = inventories.indexOf(event.getInventory());
        if (event.getSlot() == LEFT_BUTTON && inventoryIndex > 0) {
            open(inventoryIndex - 1);
        } else if (event.getSlot() == RIGHT_BUTTON && inventoryIndex < inventories.size() - 1) {
            open(inventoryIndex + 1);
        }
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInventoryDrag(InventoryDragEvent event) {

        if (!event.getWhoClicked().equals(holder.getPlayer()) || !inventories.contains(event.getInventory())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onItemDrop(InventoryInteractEvent event) {

        if (!event.getWhoClicked().equals(holder.getPlayer()) || !inventories.contains(event.getInventory())) {
            return;
        }
        event.setCancelled(true);
    }

    private void createInventory() {

        createInventory(inventories.size());
    }

    private Inventory createInventory(int id) {

        Inventory inventory = Bukkit.createInventory(null, 54, "Quest Inventar - Seite " + (id + 1));
        inventories.add(id, inventory);
        for (int i = 0; i < inventories.size(); i++) {
            Inventory inv = inventories.get(i);
            if (i > 0) {
                ItemStack itemStack = leftButton.clone();
                ItemMeta meta = itemStack.getItemMeta();
                if (i == 1) {
                    meta.setDisplayName("Zur ersten Seite wechseln");
                } else {
                    meta.setDisplayName("Zu Seite " + i + " wechseln");
                }
                itemStack.setItemMeta(meta);
                inv.setItem(LEFT_BUTTON, itemStack);
            } else {
                inv.setItem(LEFT_BUTTON, filler.clone());
            }
            if (i < inventories.size() - 1) {
                // set a right button
                ItemStack itemStack = rightButton.clone();
                ItemMeta meta = itemStack.getItemMeta();
                if (i == inventories.size() - 2) {
                    meta.setDisplayName("Zur letzen Seite wechseln");
                } else {
                    meta.setDisplayName("Zu Seite " + (i + 2) + " wechseln");
                }
                itemStack.setItemMeta(meta);
                inv.setItem(RIGHT_BUTTON, itemStack);
            } else {
                inv.setItem(RIGHT_BUTTON, filler.clone());
            }
            if (i > LEFT_BUTTON && i < RIGHT_BUTTON) {
                inv.setItem(i, filler.clone());
            }
        }
        return inventory;
    }

    private void removeInventory(Inventory inventory) {

        if (inventories.size() > 1 && inventories.remove(inventory)) {
            inventory.clear();
            Inventory lastInventory = inventories.get(inventories.size() - 1);
            lastInventory.setItem(RIGHT_BUTTON, filler);
        }
    }

    private boolean isInventoryFull(Inventory inventory) {

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (slot >= LEFT_BUTTON) return true;
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType() == Material.AIR) return false;
        }
        return true;
    }

    private boolean isInventoryEmpty(Inventory inventory) {

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (slot >= LEFT_BUTTON) return true;
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) return false;
        }
        return true;
    }

    public void addItem(ItemStack... items) {

        for (Inventory inventory : inventories) {
            if (!isInventoryFull(inventory)) {
                HashMap<Integer, ItemStack> map = inventory.addItem(items);
                if (map.isEmpty()) return;
                Collection<ItemStack> values = map.values();
                addItem(values.toArray(new ItemStack[0]));
                return;
            }
        }
        // all inventories were full so we need to create a new one
        createInventory();
        addItem(items);
    }

    public void removeItem(ItemStack... itemStack) {

        List<Inventory> emptyInventories = new ArrayList<>();
        // remove the items beginning at the last inventory
        for (int i = inventories.size() - 1; i >= 0; i--) {
            Inventory inventory = inventories.get(i);
            HashMap<Integer, ItemStack> unremovedItems = inventory.removeItem(itemStack);
            if (isInventoryEmpty(inventory)) {
                emptyInventories.add(inventory);
            }
            if (unremovedItems.isEmpty()) break;
            itemStack = unremovedItems.values().toArray(new ItemStack[0]);
        }
        emptyInventories.forEach(this::removeInventory);
    }

    public boolean contains(ItemStack item, int amount) {

        int count = 0;
        for (Inventory inventory : inventories) {
            if (inventory.containsAtLeast(item, amount)) {
                return true;
            } else {
                if (inventory.contains(item)) {
                    // we need to count the exact amount
                    for (ItemStack itemStack : inventory.getContents()) {
                        if (item.equals(itemStack)) {
                            count += itemStack.getAmount();
                        }
                    }
                }
            }
        }
        return count >= amount;
    }

    public long count() {
        return inventories.stream()
                .flatMap(inventory -> Arrays.stream(inventory.getContents()))
                .filter(itemStack -> itemStack != null && itemStack.getType() != Material.AIR)
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    public boolean contains(String item) {

        for (Inventory inventory : inventories) {
            if (RaidCraft.getItem(item).map(inventory::contains).orElse(false)) {
                return true;
            }
        }
        return false;
    }

    private void open(int index) {

        currentInventory = index;
        open();
    }

    public void open() {

        if (inventories.isEmpty()) {
            createInventory();
        }
        Inventory inventory = inventories.get(0);
        if (currentInventory < inventories.size()) {
            inventory = inventories.get(currentInventory);
        }
        holder.getPlayer().openInventory(inventory);
    }

    public void close() {

        for (Inventory inventory : inventories) {
            inventory.getViewers().forEach(HumanEntity::closeInventory);
        }
    }

    public void save() {

        List<TQuestItem> items = new ArrayList<>();
        for (int i = 0; i < inventories.size(); i++) {
            Inventory inventory = inventories.get(i);
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                if (slot >= LEFT_BUTTON) break;
                ItemStack item = inventory.getItem(slot);
                if (item != null) {
                    int storageId = storage.storeObject(item);
                    TQuestItem dbEntry = new TQuestItem();
                    dbEntry.setInventoryId(i);
                    dbEntry.setObjectStorageId(storageId);
                    dbEntry.setSlot(slot);
                    dbEntry.setPlayer(holder.getPlayerId());
                    items.add(dbEntry);
                }
            }
        }
        RaidCraft.getDatabase(QuestPlugin.class).saveAll(items);
    }

    private void load() {

        clear();
        inventories.clear();
        EbeanServer database = RaidCraft.getDatabase(QuestPlugin.class);
        List<TQuestItem> questItems = database.find(TQuestItem.class).where().eq("player", holder.getPlayerId()).findList();
        if (!questItems.isEmpty()) {
            questItems.sort(Comparator.comparingInt(TQuestItem::getInventoryId));
            for (TQuestItem questItem : questItems) {
                try {
                    Inventory inventory;
                    if (inventories.isEmpty()) {
                        inventory = createInventory(questItem.getInventoryId());
                    } else if (questItem.getInventoryId() < inventories.size()) {
                        inventory = inventories.get(questItem.getInventoryId());
                    } else {
                        inventory = createInventory(questItem.getInventoryId());
                    }
                    inventory.setItem(questItem.getSlot(), storage.getObject(questItem.getObjectStorageId()));
                    database.delete(questItem);
                } catch (StorageException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Clears out all quest items from the quest inventory.
     */
    public void clear() {
        inventories.forEach(Inventory::clear);
        save();
    }
}
