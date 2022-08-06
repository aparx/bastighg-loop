package io.github.aparx.challenges.looping.loadable.modules.loop.projectile;

import io.github.aparx.challenges.looping.PluginConstants;
import io.github.aparx.challenges.looping.loadable.modules.block.CapturedStructure;
import io.github.aparx.challenges.looping.loadable.modules.loop.LoopModuleExtension;
import io.github.aparx.challenges.looping.loadable.modules.loop.MetadataWrapper;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * @author aparx (Vinzent Zeband)
 * @version 22:26 CET, 03.08.2022
 * @since 1.0
 */
public class LoopProjectileModule
        extends LoopModuleExtension<LoopProjectileEntity>
        implements Listener {

    public static final String META_KEY = "projectile_module";

    public static final EnumSet<EntityType> PROJECTILE_BLACKLIST =
            EnumSet.of(EntityType.SPLASH_POTION, EntityType.FIREWORK);

    @NotNull @Getter
    private final Plugin plugin;

    public LoopProjectileModule(Plugin plugin) {
        super(META_KEY);
        this.plugin = plugin;
    }

    @Override
    public MetadataWrapper allocateMetadata(ArmorStand armorStand) {
        return new MetadataWrapper(armorStand, getMetadataKey());
    }

    @Override
    public LoopProjectileEntity allocateEntity(ArmorStand armorStand) {
        return new LoopProjectileEntity(armorStand, this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplode(EntityExplodeEvent event) {
        if (isNonProcessableEventOrMoment(event)) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof Projectile)) return;
        Projectile projectile = (Projectile) entity;
        LoopProjectileEntity linked = getLinkedEntityFrom(projectile);
        if (linked == null) return;
        linked.setBlockReset(CapturedStructure.of(event.blockList()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onShoot(ProjectileHitEvent event) {
        if (isNonProcessableEventOrMoment(event)) return;
        Projectile proj = event.getEntity();
        ProjectileSource shooter = proj.getShooter();
        if (shouldIgnoreShooter(shooter)) return;
        LoopProjectileEntity linked = getLinkedEntityFrom(proj);
        if (linked == null || linked.backTransition) return;
        linked.setHitPosition(proj.getLocation().toVector());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onShoot(ProjectileLaunchEvent event) {
        if (isNonProcessableEventOrMoment(event)) return;
        Projectile proj = event.getEntity();
        ProjectileSource shooter = proj.getShooter();
        if (shouldIgnoreShooter(shooter)) return;
        LoopProjectileEntity linked = getLinkedEntityFrom(proj);
        if (linked != null) return;
        if (PROJECTILE_BLACKLIST.contains(proj.getType())) return;
        linked = spawnAndRegister(getPlugin(), event.getLocation());
        linked.setProjectile(proj);
        linked.setInitialVelocity(proj.getVelocity());
    }

    private boolean shouldIgnoreShooter(ProjectileSource shooter) {
        return shooter instanceof Player && !PluginConstants.DEBUG_MODE;
    }

}
