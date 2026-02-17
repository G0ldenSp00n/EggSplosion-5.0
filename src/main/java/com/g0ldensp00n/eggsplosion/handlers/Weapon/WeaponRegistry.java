package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import com.g0ldensp00n.eggsplosion.EggSplosion;
import com.g0ldensp00n.eggsplosion.handlers.Utils.Utils;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.DamageEffect;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.DashEffect;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.ExplosionEffect;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.KnockbackEffect;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.SonicBoomEffect;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.SoundEffect;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.SplashPotionEffect;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.TeleportEffect;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.EffectListeners.KnockbackEffectListener;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class WeaponRegistry implements CommandExecutor, TabCompleter {
        private static WeaponRegistry instance;
        private static NamespacedKey weaponIDKey = new NamespacedKey(EggSplosion.getInstance(), "weapon_id");
        private static NamespacedKey isWeaponPrimaryFireKey = new NamespacedKey(EggSplosion.getInstance(),
                        "is_weapon_primary_fire");
        private static NamespacedKey primaryFireReloadAfterKey = new NamespacedKey(EggSplosion.getInstance(),
                        "primary_fire_reloaded_after");
        private static NamespacedKey secondaryFireReloadAfterKey = new NamespacedKey(EggSplosion.getInstance(),
                        "secondary_fire_reloaded_after");
        private static NamespacedKey sneakActionReloadAfterKey = new NamespacedKey(EggSplosion.getInstance(),
                        "sneak_action_reload_after_key");

        private HashMap<NamespacedKey, Weapon> weapons;
        private Weapon defaultWeapon;

        public WeaponRegistry() {
                instance = this;

                new EffectListener();
                new ReloadAnimation();
                new UseWeaponListener();
                new KnockbackEffectListener();

                weapons = new HashMap<>();
                registerLegacyWeapons();
                registerWeapons();
        }

        protected void registerLegacyWeapons() {
                // TODO: Fire Sound Effects / Single Shared at Top
                WeaponEffect reloadSoundEffect = SoundEffect.builder()
                                .addSound(Sound.sound().source(Sound.Source.HOSTILE)
                                                .type(org.bukkit.Sound.BLOCK_CHEST_LOCKED).build())
                                .playerIsSoundSource().build();

                Weapon woodenHoe = Weapon.builder("wooden_hoe").withWeaponItemMaterial(Material.WOODEN_HOE)
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(3)
                                                .withVelocityMultiplier(2.75f)
                                                .addEffect(new ExplosionEffect(1.25f, Particle.EXPLOSION))
                                                .addEffect(SoundEffect.explosionSound())
                                                .addReloadEffect(reloadSoundEffect)
                                                .withProjectileMaterial(Material.BLUE_EGG)
                                                .build())
                                .build();
                register(woodenHoe);
                defaultWeapon = woodenHoe;

                register(Weapon.builder("stone_hoe").withWeaponItemMaterial(Material.STONE_HOE)
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(50)
                                                .withVelocityMultiplier(1.5f)
                                                .addEffect(new ExplosionEffect(2.4f, Particle.EXPLOSION))
                                                .addEffect(SoundEffect.explosionSound())
                                                .addEffect(new KnockbackEffect(2))
                                                .addReloadEffect(reloadSoundEffect)
                                                .build())
                                .build());

                register(Weapon.builder("copper_hoe").withWeaponItemMaterial(Material.COPPER_HOE)
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(12)
                                                .withVelocityMultiplier(4.2f)
                                                .addEffect(new ExplosionEffect(2.5f, Particle.EXPLOSION))
                                                .addEffect(SoundEffect.explosionSound())
                                                .addEffect(new KnockbackEffect(1))
                                                .addReloadEffect(reloadSoundEffect)
                                                .withProjectileMaterial(Material.BROWN_EGG)
                                                .build())
                                .build());

                register(Weapon.builder("iron_hoe").withWeaponItemMaterial(Material.IRON_HOE)
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(14)
                                                .withVelocityMultiplier(4.8f)
                                                .addEffect(new ExplosionEffect(2.6f, Particle.EXPLOSION))
                                                .addEffect(SoundEffect.explosionSound())
                                                .addEffect(new KnockbackEffect(1.1f))
                                                .addReloadEffect(reloadSoundEffect)
                                                .build())
                                .build());

                register(Weapon.builder("golden_hoe").withWeaponItemMaterial(Material.GOLDEN_HOE)
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(17)
                                                .withVelocityMultiplier(15.5f)
                                                .addEffect(new ExplosionEffect(1.25f, Particle.EXPLOSION))
                                                .addEffect(SoundEffect.explosionSound())
                                                .addEffect(new KnockbackEffect(0.9f))
                                                .addReloadEffect(reloadSoundEffect)
                                                .withProjectileMaterial(Material.BLUE_EGG)
                                                .build())
                                .build());

                register(Weapon.builder("diamond_hoe").withWeaponItemMaterial(Material.DIAMOND_HOE)
                                .withSecondaryAction(
                                                WeaponAction.builder().withReloadTime(84).withVelocityMultiplier(1.5f)
                                                                .addEffect(new ExplosionEffect(3f, Particle.EXPLOSION))
                                                                .addEffect(new KnockbackEffect(3f))
                                                                .addEffect(SoundEffect.explosionSound())
                                                                .addReloadEffect(reloadSoundEffect)
                                                                .withProjectileMaterial(Material.SNIFFER_EGG)
                                                                .build())
                                .build());
        }

        public void registerWeapons() {
                // TODO: Pull out some weapon actions into reusable chunks

                register(Weapon.builder("shrieker_weapon")
                                .withDisplayName(MiniMessage.miniMessage()
                                                .deserialize("<gradient:#041820:#61c3cb>Sculked Hoe</gradient>"))
                                .withWeaponItemMaterial(Material.DIAMOND_HOE)
                                .withPrimaryAction(WeaponAction.builder().withReloadTime(75)
                                                .withVelocityMultiplier(2.75f)
                                                .addCastEffect(new SonicBoomEffect(Particle.SONIC_BOOM, 2, 30,
                                                                Arrays.asList(new DamageEffect(3.0, 20.0f,
                                                                                DamageType.SONIC_BOOM, false))))
                                                .addCastEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.HOSTILE)
                                                                .type(org.bukkit.Sound.ENTITY_WARDEN_SONIC_BOOM)
                                                                .volume(3.0f).build()).playerIsSoundSource().build())
                                                .build())
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(3)
                                                .withVelocityMultiplier(2.75f)
                                                .withProjectileMaterial(Material.ECHO_SHARD)
                                                .addEffect(new ExplosionEffect(1.25f, Particle.EXPLOSION))
                                                .addEffect(SoundEffect.explosionSound())
                                                .build())
                                .build());

                // TODO: Sound Effects on Secondary Action
                register(Weapon.builder("spartan_laser")
                                .withDisplayName(
                                                MiniMessage.miniMessage().deserialize(
                                                                "<gradient:#de0b0b:#ffe8e8>Spartan Laser</gradient>"))
                                .withWeaponItemMaterial(Material.STONE_HOE)
                                .withPrimaryAction(WeaponAction.builder().withReloadTime(200)
                                                .addCastEffect(new SonicBoomEffect(null, 2, 25, 5,
                                                                Arrays.asList(new ExplosionEffect(2.5f,
                                                                                Particle.EXPLOSION))))
                                                .addCastEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.HOSTILE)
                                                                .type(org.bukkit.Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE)
                                                                .volume(3.0f).build()).build())
                                                .build())
                                .withSecondaryAction(
                                                WeaponAction.builder().withReloadTime(10)
                                                                .withProjectileMaxTicksLived(10)
                                                                .withVelocityMultiplier(1.5f)
                                                                .withProjectileMaterial(Material.TURTLE_EGG)
                                                                .addEffect(new ExplosionEffect(2.4f,
                                                                                Particle.EXPLOSION))
                                                                .addEffect(new KnockbackEffect(1.1f))
                                                                .addEffect(SoundEffect.explosionSound())
                                                                .build())
                                .build());

                // TODO: Sound Effects
                register(Weapon.builder("dashing_hoe")
                                .withDisplayName(
                                                MiniMessage.miniMessage().deserialize(
                                                                "<gradient:#e69d0b:#fcbe42>Dashing Hoe</gradient>"))
                                .withWeaponItemMaterial(Material.GOLDEN_HOE)
                                .withPrimaryAction(WeaponAction.builder().withReloadTime(25)
                                                .addCastEffect(new DashEffect(1.2f))
                                                .build())
                                .withSecondaryAction(
                                                WeaponAction.builder().withReloadTime(17).withVelocityMultiplier(15.5f)
                                                                .addEffect(new ExplosionEffect(1.25f,
                                                                                Particle.EXPLOSION))
                                                                .addEffect(new KnockbackEffect(0.9f))
                                                                .addEffect(SoundEffect.explosionSound())
                                                                .addEffect(new DamageEffect(3.0f, 20.f,
                                                                                DamageType.PLAYER_EXPLOSION, false))
                                                                .build())
                                .build());

                register(Weapon.builder("wanderer_hoe")
                                .withDisplayName(
                                                MiniMessage.miniMessage().deserialize(
                                                                "<gradient:#2C456C:#435F91>Wanderer's</gradient> <gradient:#F2C039:#CC8E29>Hoe</gradient>"))
                                .withPrimaryAction(WeaponAction.builder().withReloadTime(100)
                                                .withVelocityMultiplier(4.8f)
                                                // TODO: Wind Charge Particles (WeaponEffect Abstract Particles / Sounds
                                                // / Effects Functionality)
                                                .addEffect(new KnockbackEffect(2))
                                                .withProjectileMaterial(Material.EMERALD)
                                                .addCastEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.HOSTILE)
                                                                .type(org.bukkit.Sound.ENTITY_WANDERING_TRADER_YES)
                                                                .volume(3.0f).build()).playerIsSoundSource().build())
                                                .build())
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(14)
                                                .withVelocityMultiplier(4.8f)
                                                .addEffect(new ExplosionEffect(2.6f,
                                                                Particle.EXPLOSION))
                                                .addEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.HOSTILE)
                                                                .type(org.bukkit.Sound.BLOCK_SLIME_BLOCK_BREAK)
                                                                .volume(3.5f).pitch(0.5f).build()).playerIsSoundSource()
                                                                .withPitchRange(0.4f, 0.6f).build())
                                                .addEffect(new KnockbackEffect(1.1f))
                                                .addCastEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.HOSTILE)
                                                                .type(org.bukkit.Sound.ENTITY_LLAMA_SPIT)
                                                                .volume(3.0f).build()).playerIsSoundSource().build())
                                                .withProjectile(LlamaSpit.class)
                                                .build())
                                .withSneakAction(WeaponAction.builder().withReloadTime(22)
                                                .withVelocityMultiplier(4.8f)
                                                .addCastEffect(new DashEffect(0.9f, Particle.HAPPY_VILLAGER))
                                                .addCastEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.HOSTILE)
                                                                .type(org.bukkit.Sound.ENTITY_WANDERING_TRADER_REAPPEARED)
                                                                .volume(3.0f).build()).playerIsSoundSource().build())
                                                .build())

                                .build());

                register(Weapon.builder("ender_hoe")
                                .withDisplayName(
                                                MiniMessage.miniMessage().deserialize(
                                                                "<gradient:#316364:#4E8386>The</gradient> <gradient:#B5E45A:#579143>Eye</gradient>"))
                                .withWeaponItemMaterial(Material.GRASS_BLOCK)
                                .withPrimaryAction(
                                                WeaponAction.builder().withReloadTime(100).withVelocityMultiplier(2.8f)
                                                                .addEffect(new TeleportEffect())
                                                                .withProjectileMaxTicksLived(8)
                                                                .addCastEffect(SoundEffect.builder().addSound(Sound
                                                                                .sound()
                                                                                .source(Sound.Source.HOSTILE)
                                                                                .type(org.bukkit.Sound.ENTITY_ENDER_PEARL_THROW)
                                                                                .volume(3.0f).build())
                                                                                .playerIsSoundSource().build())
                                                                .withProjectileMaterial(Material.ENDER_PEARL)
                                                                .build())
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(14)
                                                .withVelocityMultiplier(4.8f)
                                                .addEffect(new ExplosionEffect(2.6f, Particle.EXPLOSION))
                                                .addEffect(new KnockbackEffect(1.1f))
                                                .addEffect(SoundEffect.explosionSound())
                                                .withProjectileMaterial(Material.ENDER_EYE)
                                                .addCastEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.HOSTILE)
                                                                .type(org.bukkit.Sound.ENTITY_ENDERMAN_SCREAM)
                                                                .volume(3.0f).build()).playerIsSoundSource().build())
                                                .build())
                                .build());

                ArrayList<PotionEffect> potionEffects = new ArrayList<>();
                potionEffects.add(new PotionEffect(PotionEffectType.REGENERATION, 5, 4));
                register(Weapon.builder("allay_hoe")
                                .withDisplayName(
                                                MiniMessage.miniMessage().deserialize(
                                                                "<gradient:#FFFFFF:#6CC2F2>Allay Hoe</gradient>"))
                                .withWeaponItemMaterial(Material.AMETHYST_SHARD)
                                .withPrimaryAction(WeaponAction.builder().withReloadTime(100)
                                                .withVelocityMultiplier(2.8f)
                                                .addCastEffect(new SplashPotionEffect(potionEffects, 5)
                                                                .affectsEnemyTeam(false).affectsOwnTeam(true))
                                                .addCastEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.HOSTILE)
                                                                .type(org.bukkit.Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM)
                                                                .volume(3.0f).build()).playerIsSoundSource().build())
                                                .build())
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(14)
                                                .withVelocityMultiplier(4.8f)
                                                .addEffect(new ExplosionEffect(2.6f, Particle.EXPLOSION))
                                                .addEffect(new KnockbackEffect(1.1f))
                                                .addEffect(SoundEffect.explosionSound())
                                                .withProjectileMaterial(Material.AMETHYST_SHARD)
                                                .addCastEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.HOSTILE)
                                                                .type(org.bukkit.Sound.ENTITY_ALLAY_ITEM_GIVEN)
                                                                .volume(3.0f).build()).playerIsSoundSource().build())
                                                .build())
                                .build());

        }

        public static WeaponRegistry getInstance() {
                return instance;
        }

        public static NamespacedKey getWeaponIDKey() {
                return weaponIDKey;
        }

        public static NamespacedKey getIsWeaponPrimaryFireKey() {
                return isWeaponPrimaryFireKey;
        }

        public static NamespacedKey getPrimaryFireReloadAfterKey() {
                return primaryFireReloadAfterKey;
        }

        public static NamespacedKey getSecondaryFireReloadAfterKey() {
                return secondaryFireReloadAfterKey;
        }

        public static NamespacedKey getSneakActionReloadAfterKey() {
                return sneakActionReloadAfterKey;
        }

        public void register(Weapon weapon) {
                this.weapons.put(weapon.weaponID, weapon);
        }

        public Weapon getWeaponByID(NamespacedKey key) {
                return weapons.getOrDefault(key, defaultWeapon);
        }

        @Override
        public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String commandLabel,
                        @NotNull String @NotNull [] args) {
                if (commandLabel.equalsIgnoreCase("weapon")) {
                        if (sender instanceof Player) {
                                Player player = (Player) sender;
                                if (args.length == 0) {
                                        NamespacedKey woodenHoeID = new NamespacedKey(EggSplosion.getInstance(),
                                                        "wooden_hoe");
                                        player.give(getWeaponByID(woodenHoeID).getItem());

                                        NamespacedKey stoneHoeID = new NamespacedKey(EggSplosion.getInstance(),
                                                        "stone_hoe");
                                        player.give(getWeaponByID(stoneHoeID).getItem());

                                        NamespacedKey copperHoeID = new NamespacedKey(EggSplosion.getInstance(),
                                                        "copper_hoe");
                                        player.give(getWeaponByID(copperHoeID).getItem());
                                } else if (args.length == 1) {
                                        NamespacedKey weaponID = NamespacedKey.fromString(args[0]);
                                        Weapon weapon = getWeaponByID(weaponID);
                                        if (weapon != null) {
                                                player.give(weapon.getItem());
                                        } else {
                                                player.sendMessage(MiniMessage.miniMessage().deserialize(
                                                                "No weapon with ID <weapon_id>",
                                                                Placeholder.component("weapon_id",
                                                                                MiniMessage.miniMessage().deserialize(
                                                                                                weaponID.asString()))));
                                        }
                                }

                        }
                }
                return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
                if (cmd.getName().equalsIgnoreCase("weapon")) {
                        switch (args.length) {
                                case 1:
                                        List<String> commands = new ArrayList<>();
                                        for (NamespacedKey weaponID : weapons.keySet()) {
                                                commands.add(weaponID.asString());
                                        }
                                        return Utils.FilterTabComplete(args[0], commands);
                        }
                }
                return null;
        }
}
