package io.github.aparx.challenges.looping.loadable.modules;

import io.github.aparx.challenges.looping.MessageConstants;
import io.github.aparx.challenges.looping.loadable.ChallengeModule;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * A {@code ChallengeModule} used to give immunity to all players
 * participating in a challenge being paused.
 *
 * @author aparx (Vinzent Zeband)
 * @version 06:27 CET, 01.08.2022
 * @since 1.0
 */
public final class PauseModule extends ChallengeModule implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent e) {
        if (!isPaused()) return;
        Entity entity = e.getEntity();
        if (!(entity instanceof Player)) return;
        e.setCancelled(true);
        e.setDamage(0);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodLevel(FoodLevelChangeEvent e) {
        if (!isPaused()) return;
        Entity entity = e.getEntity();
        if (!(entity instanceof Player)) return;
        e.setCancelled(true);
        e.setFoodLevel(((Player) entity).getFoodLevel());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!isPaused()) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (!isPaused()) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamageOthers(EntityDamageByEntityEvent e) {
        if (!isPaused()) return;
        Entity damager = e.getDamager();
        if (!(damager instanceof Player)) return;
        e.setCancelled(true);
        damager.sendMessage(MessageConstants.CHALLENGE_ACTION_PAUSE);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if (!isPaused()) return;
        Player player = e.getPlayer();
        e.setCancelled(true);
        if (e.getItem() != null || e.getAction() == Action.LEFT_CLICK_BLOCK) {
            player.sendMessage(MessageConstants.CHALLENGE_ACTION_PAUSE);
        }
    }

}