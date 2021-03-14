package com.g0ldensp00n.eggsplosion.commands

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.entity.Player
import com.g0ldensp00n.eggsplosion.entities.Weapon

public class CreateWeaponCommand() {
  private fun prepare(weaponName: String, explosionSize: Float, hitscan: Boolean, fuse: Float): Weapon {
    return Weapon(weaponName, explosionSize, hitscan, fuse)
  }

  companion object {
    @JvmStatic
    fun run(material: Material, weapon: Weapon): ItemStack? {
      val weaponItem = ItemStack(material)
      return weapon.serialize(weaponItem)
    }
  }

  private fun persist(player: Player, weaponItem: ItemStack) {
    player.inventory.addItem(weaponItem)
  }

  public fun execute(player: Player, weaponName: String, material: Material, explosionSize: Float, hitscan: Boolean, fuse: Float) {
    val weapon = prepare(weaponName, explosionSize, hitscan, fuse)
    CreateWeaponCommand.run(material, weapon)?.let { weaponItem ->
      persist(player, weaponItem)
    }
  }
}
