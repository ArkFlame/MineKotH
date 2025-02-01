package com.arkflame.minekoth.setup.listeners;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Position;
import com.arkflame.minekoth.lang.Lang;
import com.arkflame.minekoth.menus.KothEditMenu;
import com.arkflame.minekoth.setup.commands.SetupCommand;
import com.arkflame.minekoth.setup.session.SetupSession;
import com.arkflame.minekoth.utils.Locations;

public class SetupInteractListener implements Listener {
    private Map<Player, Long> lastInteract = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        SetupSession session = MineKoth.getInstance().getSessionManager().getSession(player);
        if (session == null) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }

        event.setCancelled(true);

        if (lastInteract.containsKey(player) && System.currentTimeMillis() - lastInteract.get(player) < 500) {
            return;
        } else {
            lastInteract.put(player, System.currentTimeMillis());
        }

        Lang lang = MineKoth.getInstance().getLangManager().getLang(player);

        if (!session.isFirstPositionSet()) {
            Location loc = event.getClickedBlock().getLocation();
            session.setFirstPosition(new Position(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            session.setWorldName(loc.getWorld().getName());
            player.sendMessage(lang.getMessage("messages.position1-set").replace("<value>", Locations.toString(loc)));
        } else if (!session.isSecondPositionSet()) {
            Location loc = event.getClickedBlock().getLocation();
            if (!session.isValidPosition(loc)) {
                player.sendMessage(lang.getMessage("messages.invalid-world"));
                return;
            }
            session.setSecondPosition(new Position(loc.getBlockX(), session.getFirstPosition().getY(), loc.getBlockZ()));
            player.sendMessage(lang.getMessage("messages.position2-set").replace("<value>", Locations.toString(loc)));
            Position first = session.getFirstPosition();
            Position second = session.getSecondPosition();
            player.sendMessage(lang.getMessage("messages.area-size")
                    .replace("<x_length>", String.valueOf(first.getXLength(second)))
                    .replace("<z_length>", String.valueOf(first.getZLength(second)))
                    .replace("<area>", String.valueOf(first.getArea(second))));
            if (!session.isNameSet()) {
                player.sendMessage(lang.getMessage("messages.enter-name"));
            } else if (!session.isTimesSet()) {
                player.sendMessage(lang.getMessage("messages.enter-times"));
            }
        }

        if (session.isEditing() && session.isComplete()) {
            SetupCommand.handleFinish(player, null);
            new KothEditMenu(player, MineKoth.getInstance().getKothManager().getKothById(session.getId())).open(player);
        }
    }
}
