package com.g0ldensp00n.eggsplosion.services

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
  }
}
