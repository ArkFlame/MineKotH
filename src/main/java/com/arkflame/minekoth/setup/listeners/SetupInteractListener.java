package com.arkflame.minekoth.setup.listeners;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Position;
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

        if (!session.isFirstPositionSet()) {
            Location loc = event.getClickedBlock().getLocation();
            session.setFirstPosition(new Position(loc.getX(), loc.getY(), loc.getZ()));
            session.setWorldName(loc.getWorld().getName());
            player.sendMessage(ChatColor.GREEN + "Position 1 set at: " + ChatColor.AQUA + Locations.toString(loc));
        } else if (!session.isSecondPositionSet()) {
            Location loc = event.getClickedBlock().getLocation();
            if (!session.isValidPosition(loc)) {
                player.sendMessage(ChatColor.RED + "The world must be the same as the first position.");
                return;
            }
            session.setSecondPosition(new Position(loc.getX(), loc.getY(), loc.getZ()));
            player.sendMessage(ChatColor.GREEN + "Position 2 set at: " + ChatColor.AQUA + Locations.toString(loc));
            // Send message with size of area Area Size (22x22)
            Position first = session.getFirstPosition();
            Position second = session.getSecondPosition();
            player.sendMessage(ChatColor.GREEN + "Area size (" + first.getXLength(second) + "x" + first.getZLength(second) + "): " + ChatColor.AQUA + first.getArea(second) + " blocks");
            if (!session.isNameSet()) {
                player.sendMessage(ChatColor.GREEN + "Enter the name of the koth.");
            } else if (!session.isTimesSet()) {
                player.sendMessage(ChatColor.GREEN + "Enter the times to run the koth (e.g., 8pm 9pm 10pm).");
            }
        }

        
        if (session.isEditing() && session.isComplete()) {
            SetupCommand.handleFinish(player, null);
            new KothEditMenu(MineKoth.getInstance().getKothManager().getKothById(session.getId())).open(player);
        }
    }

}
