package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class WeaponEffect {
  public abstract void activateEffect(Location location, Player shooter);
}
