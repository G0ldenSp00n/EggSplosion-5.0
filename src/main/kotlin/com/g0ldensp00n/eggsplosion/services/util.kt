package com.g0ldensp00n.eggsplosion.services

import org.bukkit.inventory.ItemStack

public class Util() {
  companion object {
    @JvmStatic
    fun getLoreValue(key: String, lore: List<String>): String? {
      lore?.let {
        lore.forEach { loreItem -> 
          loreItem.split(":".toRegex())?.let { lorePair ->
            if (lorePair.getOrNull(0) == key) {
              return lorePair.getOrNull(1)
            }
          }
        }
      }
      return null
    }

    @JvmStatic
    fun getLoreValue(key: String, itemStack: ItemStack): String? {
      itemStack.itemMeta?.let { itemMeta ->
        itemMeta.lore?.let { lore ->
          return Util.getLoreValue(key, lore)
        }
      }
      return null
    }
  }
}
