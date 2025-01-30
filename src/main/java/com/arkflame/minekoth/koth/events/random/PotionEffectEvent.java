package com.arkflame.minekoth.koth.events.random;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import org.bukkit.entity.Player;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.particles.ParticleUtil;
import com.arkflame.minekoth.utils.FoliaAPI;
import com.arkflame.minekoth.utils.PotionEffectUtil;
import com.arkflame.minekoth.utils.Sounds;
import com.arkflame.minekoth.utils.Titles;

public class PotionEffectEvent extends RandomEvent {
    private final List<String> potentialEffects;

    public PotionEffectEvent(double chance) {
        super("PotionEffect", chance);
        this.potentialEffects = initializePotionEffects();
    }

    @Override
    public void execute(KothEvent event) {
        if (!hasPlayersInside(event))
            return;

        // Get 2-3 random effects
        List<String> selectedEffects = new ArrayList<>();
        Random random = new Random();
        int effectCount = random.nextInt(2) + 2; // 2 or 3 effects

        for (int i = 0; i < effectCount; i++) {
            selectedEffects.add(potentialEffects.get(random.nextInt(potentialEffects.size())));
        }

        // Apply effects to all players inside
        for (Player player : event.getPlayersInZone()) {
            FoliaAPI.runTaskForRegion(player.getLocation(), () -> {
                for (String effect : selectedEffects) {
                    PotionEffectUtil.applyAllValidEffects(player, 0, 600, effect);
                }
                Sounds.play(player, 1.0f, 1.0f, "ENTITY_WITCH_DRINK");
                ParticleUtil.generateCircle(player.getLocation().add(0, 1, 0), "SPELL_WITCH", 1.0, 30);
            });
            Titles.sendTitle(player, "§6Potion Effect", "§7You have been given random potion effects!", 5, 20, 5);
        }
    }

    private List<String> initializePotionEffects() {
        List<String> effects = new ArrayList<>();
        // Duration: 30 seconds (600 ticks)
        // Instead add names
        effects.add("INCREASE_DAMAGE");
        effects.add("SPEED");
        effects.add("DAMAGE_RESISTANCE");
        effects.add("REGENERATION");
        effects.add("JUMP");
        return effects;
    }

    private boolean hasPlayersInside(KothEvent event) {
        return !event.getPlayersInZone().isEmpty();
    }
}