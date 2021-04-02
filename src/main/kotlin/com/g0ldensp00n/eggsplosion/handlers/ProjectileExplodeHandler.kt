package com.g0ldensp00n.eggsplosion.handlers

import com.g0ldensp00n.eggsplosion.commands.CreateExplosionCommand
import com.g0ldensp00n.eggsplosion.handlers.ListenerHandler
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.EventHandler
import org.bukkit.Bukkit

public class ProjectileExplodeHandler (val createExplosionCommand: CreateExplosionCommand): ListenerHandler() {
  @EventHandler
  public fun onProjectileHitEvent(projectileHitEvent: ProjectileHitEvent) {
    Bukkit.getLogger().info("entity death")
    projectileHitEvent?.entity?.getMetadata("function")?.let { functionMeta ->
      if (functionMeta.size > 0) {
        functionMeta.getOrNull(0)?.asString()?.let { functionString ->
          if (functionString == "weaponProjectile") {
            createExplosionCommand.execute(projectileHitEvent?.entity)
          }
        }
      }
    }
  }
}
