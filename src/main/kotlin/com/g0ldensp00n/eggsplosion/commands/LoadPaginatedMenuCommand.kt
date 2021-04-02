package com.g0ldensp00n.eggsplosion.commands

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Inventory
import org.bukkit.entity.Player

private data class LoadPaginatedMenuPrepareResponse (val pageContents: Array<ItemStack?>, val previousPage: Boolean, val nextPage: Boolean)

public class LoadPaginatedMenuCommand {
  private fun prepare(items: ArrayList<ItemStack>, inventory: Inventory, page: Int): LoadPaginatedMenuPrepareResponse {
    val itemsPerPage = inventory.size - 2
    val finalPage = itemsPerPage*(page+1) > items.size
    val highestItemOnPage = if(finalPage) items.size else itemsPerPage*(page+1)
    val pageContents = Array<ItemStack?>(27, {
      i -> null
    })
    val pageContentsList = ArrayList(items.slice(itemsPerPage*page..highestItemOnPage)).toArray(pageContents)

    return LoadPaginatedMenuPrepareResponse(pageContents, page > 0, !finalPage)
  }

  companion object {
    @JvmStatic
    public fun run(pageContents: Array<ItemStack?>, inventory: Inventory) {
      inventory.contents = pageContents
    }
  }

  private fun persist(inventory: Inventory, player: Player) {
    player.openInventory(inventory)
  }


  public fun execute(items: ArrayList<ItemStack>, inventory: Inventory, page: Int, player: Player) {
    val prepareResponse = prepare(items, inventory, page)
    LoadPaginatedMenuCommand.run(prepareResponse.pageContents, inventory)
    persist(inventory, player)
  }
}
