package com.arkflame.minekoth.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Consumer;

public class MenuUtil {
    private static final Map<UUID, Menu> openMenus = new HashMap<>();

    public static void registerEvents(org.bukkit.plugin.Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(new MenuListener(), plugin);
    }

    public static class Menu {
        private final String title;
        private final int size;
        private final Map<Integer, MenuItem> items;
        private ItemStack background;
        private final int margin;

        public Menu(String title, int rows) {
            this.title = title;
            this.size = rows * 9;
            this.items = new HashMap<>();
            this.margin = 1;
        }

        public void setItem(int slot, MenuItem item) {
            items.put(slot, item);
        }

        public void setBackground(ItemStack background) {
            this.background = background;
        }

        public void setBackground(Material background) {
            setBackground(new ItemStack(background));
        }

        public void grid(MenuItem... items) {
            int startSlot = 11; // First slot after margin
            int currentSlot = startSlot;

            for (MenuItem item : items) {
                // Skip if we're at the right margin
                if ((currentSlot + 1) % 9 == 0) {
                    currentSlot += 2;
                }

                // Skip to next row if we're at the end
                if (currentSlot >= size - 9) {
                    break;
                }

                setItem(currentSlot, item);
                currentSlot += 2; // Skip one slot for spacing
            }
        }

        public void open(Player player) {
            Inventory inventory = Bukkit.createInventory(null, size, title);

            // Set background first if exists
            if (background != null) {
                for (int i = 0; i < size; i++) {
                    inventory.setItem(i, background);
                }
            }

            // Set menu items
            for (Map.Entry<Integer, MenuItem> entry : items.entrySet()) {
                if (entry.getKey() >= size) {
                    break;
                }
                inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
            }
            openMenus.put(player.getUniqueId(), this);
            FoliaAPI.runTask(() -> {
                player.openInventory(inventory);
            });
        }

        public void handleClick(InventoryClickEvent event) {
            MenuItem item = items.get(event.getSlot());
            if (item != null && item.getClickHandler() != null) {
                item.getClickHandler().accept(event);
            }
        }
    }

    public static class MenuItem {
        private final ItemStack itemStack;
        private final Consumer<InventoryClickEvent> clickHandler;

        private MenuItem(ItemStack itemStack, Consumer<InventoryClickEvent> clickHandler) {
            this.itemStack = itemStack;
            this.clickHandler = clickHandler;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public Consumer<InventoryClickEvent> getClickHandler() {
            return clickHandler;
        }

        public static class Builder {
            private final ItemStack itemStack;
            private Consumer<InventoryClickEvent> clickHandler;

            public Builder(Material material) {
                this.itemStack = new ItemStack(material);
            }

            public Builder name(String name) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(name);
                    itemStack.setItemMeta(meta);
                }
                return this;
            }

            public Builder lore(String... lore) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.setLore(Arrays.asList(lore));
                    itemStack.setItemMeta(meta);
                }
                return this;
            }

            public Builder onClick(Consumer<InventoryClickEvent> clickHandler) {
                this.clickHandler = clickHandler;
                return this;
            }

            public MenuItem build() {
                return new MenuItem(itemStack, clickHandler);
            }
        }
    }

    private static class MenuListener implements Listener {
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            Player player = (Player) event.getWhoClicked();
            Menu menu = openMenus.get(player.getUniqueId());

            if (menu != null) {
                event.setCancelled(true);
                menu.handleClick(event);
            }
        }

        @EventHandler
        public void onInventoryDrag(InventoryDragEvent event) {
            Player player = (Player) event.getWhoClicked();
            if (openMenus.containsKey(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            Menu menu = openMenus.get(event.getPlayer().getUniqueId());
            if (menu != null) {
                openMenus.remove(event.getPlayer().getUniqueId());
            }
        }
    }

    public static void shutdown() {
        Iterator<Map.Entry<UUID, Menu>> iterator = openMenus.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Menu> entry = iterator.next();
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                player.closeInventory();
            }
            iterator.remove();
        }
    }
}