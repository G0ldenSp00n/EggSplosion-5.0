package com.g0ldensp00n.eggsplosion.entities

import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.Color
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.Material
import org.bukkit.scheduler.BukkitTask
import org.bukkit.metadata.FixedMetadataValue
import com.g0ldensp00n.eggsplosion.entities.TeamConfig
import com.g0ldensp00n.eggsplosion.Main

public data class SpawnPoint(val position: Position) {
  private var spawnDisplay: ArmorStand? = null
  private var scheduledHideTask: BukkitTask? = null

  private fun spawnArmor(color: Color): List<ItemStack> {
    val helmet = createDyedArmor(Material.LEATHER_HELMET, color)
    val chestplate = createDyedArmor(Material.LEATHER_CHESTPLATE, color)
    val leggings = createDyedArmor(Material.LEATHER_LEGGINGS, color)
    val boots = createDyedArmor(Material.LEATHER_BOOTS, color)
    return listOf(helmet, chestplate, leggings, boots)
  }

  private fun createDyedArmor(material: Material, color: Color): ItemStack {
    val armor = ItemStack(material)
    val armorMeta = armor.itemMeta
    if (armorMeta is LeatherArmorMeta) {
      armorMeta.setColor(color)
    }
    armor.itemMeta = armorMeta
    return armor
  }

  private fun applyArmorToPlayer(armorSet: List<ItemStack>, entity: LivingEntity) {
    entity.equipment?.setHelmet(armorSet.getOrNull(0))
    entity.equipment?.setChestplate(armorSet.getOrNull(1))
    entity.equipment?.setLeggings(armorSet.getOrNull(2))
    entity.equipment?.setBoots(armorSet.getOrNull(3))
  }

  fun show(gameMap: GameMap, team: TeamConfig, hideTask: BukkitTask, plugin: Main) {
    scheduledHideTask?.let {
      it.cancel()
    }
    scheduledHideTask = hideTask

    gameMap.world?.let { world ->
      if (spawnDisplay == null) {
        val entity: Entity = world.spawnEntity(position.toLocation(world), EntityType.ARMOR_STAND);
        entity.teleport(position.toLocation(world))
        entity.setMetadata("gameMap", FixedMetadataValue(plugin, gameMap.name))
        entity.setMetadata("team", FixedMetadataValue(plugin, team.name))
        if (entity is ArmorStand) {
          entity.setGravity(false)
          entity.setInvulnerable(true)
          entity.setBasePlate(false)
          applyArmorToPlayer(spawnArmor(team.bukkitColor()), entity)
          spawnDisplay = entity;
        }
      }
    }
  }

  fun hide() {
    spawnDisplay?.let { armorStand ->
      armorStand.remove()
      spawnDisplay = null
    }
  }

  fun spawnPlayer(player: Player, world: World, color: Color) {
    hide();
    val spawnLoc = position.toLocation(world)
    player.teleport(spawnLoc)
    applyArmorToPlayer(spawnArmor(color), player)
  }
}
