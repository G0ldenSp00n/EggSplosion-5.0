package com.g0ldensp00n.eggsplosion.entities

import org.bukkit.inventory.ItemStack
import com.g0ldensp00n.eggsplosion.services.Util

public data class Weapon(val weaponName: String, val explosionSize: Float, val hitscan: Boolean, val fuse: Float) {
  companion object {
    @JvmStatic
    fun deserialize(itemStack: ItemStack): Weapon? {
      itemStack.itemMeta?.let { itemMeta ->
        itemMeta.lore?.let { lore ->
          if (Util.getLoreValue("function", lore) == "weapon") {
            Util.getLoreValue("explosionSize", lore)?.toFloat()?.let { explosionSize ->
              Util.getLoreValue("hitscan", lore)?.toBoolean()?.let { hitscan ->
                Util.getLoreValue("fuse", lore)?.toFloat()?.let { fuse ->
                  return Weapon(itemMeta.displayName, explosionSize, hitscan, fuse)
                }
              }
            }
          }
        }
      }
      return null
    }
  }

  fun serialize(itemStack: ItemStack): ItemStack? {
    itemStack.itemMeta?.let { itemMeta ->
      itemMeta.setDisplayName(weaponName)
      val lore = listOf(
        "function:weapon",
        "explosionSize:" + explosionSize.toString(),
        "hitscan:" + hitscan.toString(),
        "fuse:" + fuse.toString())
      itemMeta.lore = lore
      itemStack.itemMeta = itemMeta
      return itemStack
    }
    return null
  }
}
