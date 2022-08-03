package io.github.aparx.challenges.looping.loadable.modules.loop.loops.projectile;

import com.google.common.base.Preconditions;
import io.github.aparx.challenges.looping.loadable.modules.loop.LoopEntityMetadata;
import io.github.aparx.challenges.looping.loadable.modules.loop.LoopModuleExtension;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.Plugin;

import javax.validation.constraints.NotNull;

/**
 * @author aparx (Vinzent Zeband)
 * @version 14:33 CET, 02.08.2022
 * @since 1.0
 */
public class ProjectileLoopModule extends LoopModuleExtension<ProjectileLoopEntity> implements Listener {

    public static final String META_KEY = "proj_loop";

    @NotNull @Getter
    private final Plugin plugin;

    public ProjectileLoopModule(@NotNull Plugin plugin) {
        super(META_KEY);
        this.plugin = Preconditions.checkNotNull(plugin);
    }

    @Override
    public LoopEntityMetadata allocateMetadata(ArmorStand armorStand) {
        return new LoopEntityMetadata(armorStand, META_KEY);
    }

    @Override
    public ProjectileLoopEntity allocateEntity(ArmorStand armorStand) {
        return new ProjectileLoopEntity(armorStand, this);
    }

    @EventHandler
    public void onShoot(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        System.out.println("LAUNCH_EVENT");
        //if (shooter instanceof Player) return;
        if (getLinkedEntityFrom(projectile) != null) return;
        Location self = projectile.getLocation();
        Location location = event.getLocation();
        location.setYaw(self.getYaw());
        location.setPitch(self.getPitch());
        ProjectileLoopEntity entity = spawnAndRegister(getPlugin(), location);
        System.out.println("> LAUNCHED_NEW_PROJECTILE " + location.getYaw() + "/" + location.getPitch());
        entity.setProjectile(projectile);
        entity.setForceVector(projectile.getVelocity());
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        ProjectileLoopEntity entity = getLinkedEntityFrom(projectile);
        if (entity == null) return;
        System.out.println("ON_HIT");
        entity.setHitLocation(projectile.getLocation());
    }

}