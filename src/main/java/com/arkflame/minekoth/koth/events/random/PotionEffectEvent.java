package com.arkflame.minekoth.koth.events.random;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import org.bukkit.entity.Player;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.lang.Lang;
import com.arkflame.minekoth.particles.ParticleUtil;
import com.arkflame.minekoth.utils.PotionEffectUtil;
import com.arkflame.minekoth.utils.Sounds;
import com.arkflame.minekoth.utils.Titles;

public class PotionEffectEvent extends RandomEvent {
    private final List<String> potentialEffects;
    private final int minEffects;
    private final int maxEffects;

    public PotionEffectEvent(double chance, List<String> potentialEffects, int minEffects, int maxEffects) {
        super("PotionEffect", chance);
        this.potentialEffects = potentialEffects;
        this.minEffects = minEffects;
        this.maxEffects = maxEffects;
    }

    @Override
    public void execute(KothEvent event) {
        if (!hasPlayersInside(event))
            return;

        // Get 2-3 random effects
        List<String> selectedEffects = new ArrayList<>();
        Random random = new Random();
        int effectCount = random.nextInt(maxEffects) + minEffects;

        for (int i = 0; i < effectCount; i++) {
            selectedEffects.add(potentialEffects.get(random.nextInt(potentialEffects.size())));
        }

        // Apply effects to all players inside
        for (Player player : event.getPlayersInZone()) {
            for (String effect : selectedEffects) {
                PotionEffectUtil.applyAllValidEffects(player, 0, 600, effect);
            }
            Sounds.play(player, 1.0f, 1.0f, "ENTITY_WITCH_DRINK");
            ParticleUtil.generateCircle(player.getLocation().add(0, 1, 0), "SPELL_WITCH", 1.0, 30);
            Lang lang = MineKoth.getInstance().getLangManager().getLang(player);
            Titles.sendTitle(player,
                    lang.getMessage("messages.potion-effect-title"),
                    lang.getMessage("messages.potion-effect-subtitle"),
                    5, 20, 5);
        }
    }

    private boolean hasPlayersInside(KothEvent event) {
        return !event.getPlayersInZone().isEmpty();
    }
}
