package com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.g0ldensp00n.eggsplosion.EggSplosion;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.WeaponEffect;

import net.kyori.adventure.sound.Sound;

public class SoundEffect extends WeaponEffect {
  HashMap<Integer, Collection<Sound>> soundsToPlayAtDelay;
  boolean isPlayerSoundSound = false;
  float pitchRangeLower = 1.0f;
  float pitchRangeHigher = 1.0f;

  public SoundEffect() {
    soundsToPlayAtDelay = new HashMap<>();
  }

  public SoundEffect(HashMap<Integer, Collection<Sound>> soundsToPlayAtDelay, boolean isPlayerSoundSource,
      float pitchRangeLower, float pitchRangeHigher) {
    this.soundsToPlayAtDelay = soundsToPlayAtDelay;
    this.isPlayerSoundSound = isPlayerSoundSource;
    this.pitchRangeLower = pitchRangeLower;
    this.pitchRangeHigher = pitchRangeHigher;
  }

  public void playSound(int delayKey, Player player, Location location) {
    for (Sound sound : soundsToPlayAtDelay.getOrDefault(delayKey, new ArrayList<>())) {
      Sound.Builder pitchAlteredSound = Sound.sound(sound);
      if (pitchRangeLower != pitchRangeHigher) {
        Random random = new Random();
        if (pitchRangeLower < pitchRangeHigher) {
          pitchAlteredSound.pitch(random.nextFloat(pitchRangeLower, pitchRangeHigher));
        } else {
          pitchAlteredSound.pitch(random.nextFloat(pitchRangeHigher, pitchRangeLower));
        }
      }
      if (isPlayerSoundSound) {
        player.getWorld().playSound(pitchAlteredSound.build(), player);
      } else {
        location.getWorld().playSound(pitchAlteredSound.build(), location.getX(), location.getY(), location.getZ());
      }
    }
  }

  @Override
  public void activateEffect(Location location, Player shooter) {
    for (int tickDelay : soundsToPlayAtDelay.keySet()) {
      if (tickDelay == 0) {
        this.playSound(0, shooter, location);
      } else {
        new BukkitRunnable() {
          @Override
          public void run() {
            playSound(tickDelay, shooter, location);
          }
        }.runTaskLater(EggSplosion.getInstance(), tickDelay);
      }
    }
  }

  public static SoundEffect.Builder builder() {
    return new SoundEffect.Builder();
  }

  public static SoundEffect explosionSound() {
    return SoundEffect.builder().addSound(Sound.sound()
        .source(Sound.Source.HOSTILE)
        .type(org.bukkit.Sound.ENTITY_GENERIC_EXPLODE)
        .volume(4.0f).build()).withPitchRange(0.56f, 0.84f).build();
  }

  public static class Builder {
    HashMap<Integer, Collection<Sound>> soundsToPlayAtDelay;
    boolean isPlayerSoundSound = false;
    float pitchRangeLower = 1.0f;
    float pitchRangeHigher = 1.0f;

    public Builder() {
      this.soundsToPlayAtDelay = new HashMap<>();
    }

    public Builder addSound(Sound sound) {
      return this.addSoundWithDelay(sound, 0);
    }

    public Builder addSoundWithDelay(Sound sound, int tickDelay) {
      Collection<Sound> sounds = this.soundsToPlayAtDelay.getOrDefault(tickDelay, new ArrayList<>());
      sounds.add(sound);
      this.soundsToPlayAtDelay.put(tickDelay, sounds);
      return this;
    }

    public Builder withPlayerAsSource() {
      isPlayerSoundSound = true;
      return this;
    }

    public Builder withPitchRange(float lowerPitchRange, float upperPitchRange) {
      this.pitchRangeLower = lowerPitchRange;
      this.pitchRangeHigher = upperPitchRange;
      return this;
    }

    public SoundEffect build() {
      return new SoundEffect(soundsToPlayAtDelay, isPlayerSoundSound, pitchRangeLower, pitchRangeHigher);
    }
  }
}
