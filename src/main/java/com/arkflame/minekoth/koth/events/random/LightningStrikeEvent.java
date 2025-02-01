package com.arkflame.minekoth.koth.events.random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.arkflame.mineclans.modernlib.utils.Titles;
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.lang.Lang;

public class LightningStrikeEvent extends RandomEvent {
    public LightningStrikeEvent(double chance) {
        super("LightningStrike", chance);
    }

    @Override
    public void execute(KothEvent event) {
        if (!hasPlayersInside(event))
            return;

        Location center = event.getKoth().getCenter();
        World world = center.getWorld();

        world.strikeLightningEffect(center);

        // Damage nearby players
        for (Player player : event.getPlayersInZone()) {
            if (player.getLocation().distance(center) <= 3) {
                player.damage(1.0); // 3 hearts of damage
                player.setFireTicks(60); // Set on fire for 3 seconds
            }
            Lang lang = MineKoth.getInstance().getLangManager().getLang(player);

            Titles.sendTitle(player,
                    lang.getMessage("messages.lightning-strike-title"),
                    lang.getMessage("messages.lightning-strike-subtitle"),
                    5, 20, 5);
        }
    }

    private boolean hasPlayersInside(KothEvent event) {
        return !event.getPlayersInZone().isEmpty();
    }
}