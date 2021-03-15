package com.g0ldensp00n.eggsplosion.commands

import com.g0ldensp00n.eggsplosion.entities.Position
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.World
import org.bukkit.Sound
import org.bukkit.Particle

private data class CreateExplosionPrepareResponse(val position: Position, val world: World?, val explosionSize: Float)

public class CreateExplosionCommand() {
  private fun prepare(entity: Entity): CreateExplosionPrepareResponse? {
    entity.getMetadata("explosionSize")?.let { explosionSizeMeta ->
      if (explosionSizeMeta.size > 0) {
        explosionSizeMeta.getOrNull(0)?.asFloat()?.let { explosionSize ->
          val loc = entity.location
          val pos = Position(loc.x, loc.y, loc.z, loc.pitch, loc.yaw)
          return CreateExplosionPrepareResponse(pos, loc.world, explosionSize)
        }
      }
    }
    return null
  }

  companion object {
    @JvmStatic
    fun run(position: Position, world: World, explosionSize: Float, player: Player?) {
      val location = position.toLocation(world)
      if (player is Entity) {
        world.createExplosion(location, explosionSize, false, true, player);
      } else {
        world.createExplosion(location, explosionSize, false, true);
      }
      world.spawnParticle(Particle.EXPLOSION_HUGE, location, 0);
      world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 3f, 1f);
    }
  }

  fun execute(entity: Entity) {
    prepare(entity)?.let { prepareResponse ->
      prepareResponse?.world?.let {
        CreateExplosionCommand.run(prepareResponse.position, it, prepareResponse.explosionSize, null)
      }
    }
  }
}
