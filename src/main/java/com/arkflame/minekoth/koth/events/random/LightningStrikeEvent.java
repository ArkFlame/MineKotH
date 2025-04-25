package com.arkflame.minekoth.koth.events.random;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.lang.Lang;
import com.arkflame.minekoth.utils.Titles;
import com.arkflame.minekoth.utils.Worlds;

public class LightningStrikeEvent extends RandomEvent {
    private final int damage;
    private final int fireTicks;

    public LightningStrikeEvent(double chance, int damage, int fireTicks) {
        super("LightningStrike", chance);
        this.damage = damage;
        this.fireTicks = fireTicks;
    }

    @Override
    public void execute(KothEvent event) {
        if (!hasPlayersInside(event))
            return;

        Location center = event.getKoth().getCenter();
        Worlds.strikeLightning(center);
        // Damage nearby players
        for (Player player : event.getPlayersInZone()) {
            if (player.getLocation().distance(center) <= 3) {
                player.damage(damage);
                player.setFireTicks(fireTicks);
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
