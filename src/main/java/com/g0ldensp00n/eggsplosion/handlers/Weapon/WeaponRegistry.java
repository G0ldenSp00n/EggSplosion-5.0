package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.BlockType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.WindCharge;
import org.bukkit.entity.WitherSkull;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import com.g0ldensp00n.eggsplosion.EggSplosion;
import com.g0ldensp00n.eggsplosion.handlers.Utils.Utils;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.DamageEffect;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.DashEffect;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.DelayEffect;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.ExplosionEffect;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.GeometricVisualEffect;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.KnockbackEffect;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.SelfPotionEffect;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.SonicBoomEffect;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.SoundEffect;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.SplashPotionEffect;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.TeleportEffect;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.VisualEffect;
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
        private static NamespacedKey splitCountKey = new NamespacedKey(EggSplosion.getInstance(),
                        "projectile_split_count");

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
                WeaponEffect castSoundEffect = SoundEffect.builder()
                                .addSound(Sound.sound().source(Sound.Source.UI)
                                                .type(org.bukkit.Sound.ENTITY_EGG_THROW).build())
                                .withPlayerAsSource().build();

                WeaponEffect reloadSoundEffect = SoundEffect.builder()
                                .addSound(Sound.sound().source(Sound.Source.UI)
                                                .type(org.bukkit.Sound.BLOCK_CHEST_LOCKED).pitch(2).build())
                                .withPlayerAsSource().build();

                WeaponEffect trailEffect = VisualEffect.builder()
                                .addParticle(Particle.SMOKE.builder()
                                                .offset(0, 0, 0).extra(0))
                                .build();

                Weapon woodenHoe = Weapon.builder("wooden_hoe").withWeaponItemMaterial(Material.WOODEN_HOE)
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(3)
                                                .withVelocityMultiplier(2.75f)
                                                .addCastEffect(castSoundEffect)
                                                .addEffect(new ExplosionEffect(1.25f))
                                                .addEffect(SoundEffect.explosionSound())
                                                .addEffect(VisualEffect.explosionVisualEffect())
                                                .addReloadEffect(reloadSoundEffect)
                                                .addTrailEffect(trailEffect)
                                                .withProjectileMaterial(Material.BLUE_EGG)
                                                .build())
                                .build();
                register(woodenHoe);
                defaultWeapon = woodenHoe;

                register(Weapon.builder("stone_hoe").withWeaponItemMaterial(Material.STONE_HOE)
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(50)
                                                .withVelocityMultiplier(1.5f)
                                                .addCastEffect(castSoundEffect)
                                                .addEffect(new ExplosionEffect(2.4f))
                                                .addEffect(SoundEffect.explosionSound())
                                                .addEffect(VisualEffect.explosionVisualEffect())
                                                .addEffect(new KnockbackEffect(2))
                                                .addTrailEffect(trailEffect)
                                                .addReloadEffect(reloadSoundEffect)
                                                .build())
                                .build());

                register(Weapon.builder("copper_hoe").withWeaponItemMaterial(Material.COPPER_HOE)
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(12)
                                                .withVelocityMultiplier(4.2f)
                                                .addCastEffect(castSoundEffect)
                                                .addEffect(new ExplosionEffect(2.5f))
                                                .addEffect(SoundEffect.explosionSound())
                                                .addEffect(VisualEffect.explosionVisualEffect())
                                                .addEffect(new KnockbackEffect(1))
                                                .addTrailEffect(trailEffect)
                                                .addReloadEffect(reloadSoundEffect)
                                                .withProjectileMaterial(Material.BROWN_EGG)
                                                .build())
                                .build());

                register(Weapon.builder("iron_hoe").withWeaponItemMaterial(Material.IRON_HOE)
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(14)
                                                .withVelocityMultiplier(4.8f)
                                                .addCastEffect(castSoundEffect)
                                                .addEffect(new ExplosionEffect(2.6f))
                                                .addEffect(SoundEffect.explosionSound())
                                                .addEffect(VisualEffect.explosionVisualEffect())
                                                .addEffect(new KnockbackEffect(1.1f))
                                                .addTrailEffect(trailEffect)
                                                .addReloadEffect(reloadSoundEffect)
                                                .build())
                                .build());

                register(Weapon.builder("golden_hoe").withWeaponItemMaterial(Material.GOLDEN_HOE)
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(17)
                                                .withVelocityMultiplier(15.5f)
                                                .addCastEffect(castSoundEffect)
                                                .addEffect(new ExplosionEffect(1.25f))
                                                .addEffect(SoundEffect.explosionSound())
                                                .addEffect(VisualEffect.explosionVisualEffect())
                                                .addEffect(new KnockbackEffect(0.9f))
                                                .addEffect(new DamageEffect(3.0f, 20.f,
                                                                DamageType.PLAYER_EXPLOSION, false))
                                                .addTrailEffect(trailEffect)
                                                .addReloadEffect(reloadSoundEffect)
                                                .withProjectileMaterial(Material.BLUE_EGG)
                                                .build())
                                .build());

                register(Weapon.builder("diamond_hoe").withWeaponItemMaterial(Material.DIAMOND_HOE)
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(84)
                                                .withVelocityMultiplier(1.5f)
                                                .addCastEffect(castSoundEffect)
                                                .addEffect(new ExplosionEffect(3f))
                                                .addEffect(new KnockbackEffect(3f))
                                                .addEffect(SoundEffect.explosionSound())
                                                .addEffect(VisualEffect.explosionVisualEffect())
                                                .addTrailEffect(trailEffect)
                                                .addReloadEffect(reloadSoundEffect)
                                                .withProjectileMaterial(Material.SNIFFER_EGG)
                                                .build())
                                .build());

                register(Weapon.builder("netherite_hoe").withWeaponItemMaterial(Material.NETHERITE_HOE)
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(800)
                                                .withVelocityMultiplier(3.f)
                                                .withProjectileSplittingCount(3).withMaxProjectileSplits(4)
                                                .addEffect(new ExplosionEffect(3f))
                                                .addEffect(new KnockbackEffect(3f))
                                                .addEffect(SoundEffect.explosionSound())
                                                .addEffect(VisualEffect.explosionVisualEffect())
                                                .addTrailEffect(trailEffect)
                                                .addReloadEffect(reloadSoundEffect)
                                                .withProjectileMaterial(Material.DRAGON_EGG)
                                                .build())
                                .build());
        }

        public void registerWeapons() {
                register(Weapon.builder("sculked_hoe")
                                .withDisplayName(MiniMessage.miniMessage()
                                                .deserialize("<gradient:#041820:#61c3cb>Sculked Hoe</gradient>"))
                                .withWeaponItemMaterial(Material.DIAMOND_HOE)
                                .withSneakAction(WeaponAction.builder().withReloadTime(600)
                                                .addCastEffect(VisualEffect.builder()
                                                                .addParticle(Particle.SHRIEK.builder().data(0))
                                                                .addParticleWithDelay(Particle.SHRIEK.builder().data(0),
                                                                                10)
                                                                .addParticleWithDelay(Particle.SHRIEK.builder().data(0),
                                                                                20)
                                                                .addParticleWithDelay(Particle.SHRIEK.builder().data(0),
                                                                                30)
                                                                .withPlayerAsSource()
                                                                .build())
                                                .addCastEffect(SoundEffect.builder().addSound(
                                                                Sound.sound()
                                                                                .source(Sound.Source.HOSTILE)
                                                                                .type(org.bukkit.Sound.BLOCK_SCULK_SHRIEKER_SHRIEK)
                                                                                .volume(3.0f).build())
                                                                .addSoundWithDelay(Sound.sound()
                                                                                .source(Sound.Source.HOSTILE)
                                                                                .type(org.bukkit.Sound.ENTITY_WARDEN_NEARBY_CLOSEST)
                                                                                .volume(3.0f).build(), 150)
                                                                .withPlayerAsSource().build())
                                                .addCastEffect(new SplashPotionEffect(Arrays.asList(
                                                                PotionEffectType.DARKNESS.createEffect(240, 1)), 50.f)
                                                                .affectsOwnTeam(true).withHiddenSplashEffect())
                                                .addReloadEffect(SoundEffect.builder().addSound(
                                                                Sound.sound()
                                                                                .source(Sound.Source.UI)
                                                                                .type(org.bukkit.Sound.ENTITY_WARDEN_HEARTBEAT)
                                                                                .volume(3.0f).build())
                                                                .withPlayerAsSource().build())
                                                .build())
                                .withPrimaryAction(WeaponAction.builder().withReloadTime(640)
                                                .withVelocityMultiplier(2.75f)
                                                .addCastEffect(new DelayEffect(30, Arrays.asList(new SonicBoomEffect(
                                                                Particle.SONIC_BOOM, 2, 30,
                                                                Arrays.asList(new DamageEffect(4.5f, 40.0f,
                                                                                DamageType.SONIC_BOOM, false))))))
                                                .addCastEffect(SoundEffect.builder().addSound(
                                                                Sound.sound()
                                                                                .source(Sound.Source.HOSTILE)
                                                                                .type(org.bukkit.Sound.ENTITY_WARDEN_SONIC_CHARGE)
                                                                                .volume(3.0f).build())
                                                                .addSoundWithDelay(Sound.sound()
                                                                                .source(Sound.Source.HOSTILE)
                                                                                .type(org.bukkit.Sound.ENTITY_WARDEN_SONIC_BOOM)
                                                                                .volume(3.0f).build(), 35)
                                                                .withPlayerAsSource().build())
                                                .build())
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(12)
                                                .withVelocityMultiplier(4.0f)
                                                .withProjectileMaterial(Material.ECHO_SHARD)
                                                .addEffect(new ExplosionEffect(2.2f))
                                                .addEffect(SoundEffect.explosionSound())
                                                .addEffect(GeometricVisualEffect.builder()
                                                                .withParticleBuilder(
                                                                                Particle.SCULK_CHARGE_POP.builder())
                                                                .withParticleCount(200)
                                                                .withShape(GeometricVisualEffect.Shape.Sphere.builder()
                                                                                .withOffset(new GeometricVisualEffect.Offset.FromPoint()
                                                                                                .withSpeed(0.2f))
                                                                                .withRadius(.5f).build())
                                                                .build())
                                                .addEffect(GeometricVisualEffect.builder()
                                                                .withParticleBuilder(Particle.SCULK_SOUL.builder())
                                                                .withParticleCount(150)
                                                                .withShape(GeometricVisualEffect.Shape.Sphere.builder()
                                                                                .withOffset(new GeometricVisualEffect.Offset.FromPoint()
                                                                                                .withSpeed(0.5f))
                                                                                .withRadius(1.f).build())
                                                                .build())
                                                .addTrailEffect(VisualEffect.builder()
                                                                .addParticle(Particle.ITEM.builder()
                                                                                .data(ItemType.ECHO_SHARD
                                                                                                .createItemStack())
                                                                                .offset(0, 0, 0).extra(0))
                                                                .build())
                                                .build())
                                .build());

                register(Weapon.builder("spartan_laser")
                                .withDisplayName(
                                                MiniMessage.miniMessage().deserialize(
                                                                "<gradient:#de0b0b:#ffe8e8>Spartan Laser</gradient>"))
                                .withWeaponItemMaterial(Material.STONE_HOE)
                                .withPrimaryAction(WeaponAction.builder().withReloadTime(580)
                                                .addCastEffect(new DelayEffect(38,
                                                                Arrays.asList(new SonicBoomEffect(null, 2, 25, 5,
                                                                                Arrays.asList(new ExplosionEffect(2.5f),
                                                                                                GeometricVisualEffect
                                                                                                                .builder()
                                                                                                                .withParticleBuilder(
                                                                                                                                Particle.LARGE_SMOKE
                                                                                                                                                .builder())
                                                                                                                .withParticleCount(
                                                                                                                                150)
                                                                                                                .withShape(GeometricVisualEffect.Shape.Sphere
                                                                                                                                .builder()
                                                                                                                                .withOffset(new GeometricVisualEffect.Offset.FromPoint()
                                                                                                                                                .withSpeed(0.2f))
                                                                                                                                .withRadius(.5f)
                                                                                                                                .build())
                                                                                                                .build(),
                                                                                                VisualEffect.explosionVisualEffect())))))
                                                .addCastEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.HOSTILE)
                                                                .type(org.bukkit.Sound.BLOCK_BEACON_AMBIENT)
                                                                .pitch(0.5f)
                                                                .volume(3.0f).build()).addSoundWithDelay(Sound.sound()
                                                                                .source(Sound.Source.UI)
                                                                                .type(org.bukkit.Sound.ENTITY_GUARDIAN_ATTACK)
                                                                                .pitch(0.7f)
                                                                                .volume(3.0f).build(), 4)
                                                                .addSoundWithDelay(Sound.sound()
                                                                                .source(Sound.Source.UI)
                                                                                .type(org.bukkit.Sound.ENTITY_GUARDIAN_ATTACK)
                                                                                .pitch(1.0f)
                                                                                .volume(3.0f).build(), 8)
                                                                .addSoundWithDelay(Sound.sound()
                                                                                .source(Sound.Source.UI)
                                                                                .type(org.bukkit.Sound.ENTITY_GUARDIAN_ATTACK)
                                                                                .pitch(1.3f)
                                                                                .volume(3.0f).build(), 12)
                                                                .addSoundWithDelay(Sound.sound()
                                                                                .source(Sound.Source.UI)
                                                                                .type(org.bukkit.Sound.ENTITY_GUARDIAN_ATTACK)
                                                                                .pitch(1.6f)
                                                                                .volume(3.0f).build(), 16)
                                                                .addSoundWithDelay(Sound.sound()
                                                                                .source(Sound.Source.UI)
                                                                                .type(org.bukkit.Sound.ENTITY_GUARDIAN_ATTACK)
                                                                                .pitch(2.0f)
                                                                                .volume(3.0f).build(), 20)
                                                                .addSoundWithDelay(Sound.sound()
                                                                                .source(Sound.Source.UI)
                                                                                .type(org.bukkit.Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE)
                                                                                .volume(3.0f).build(), 35)
                                                                .addSoundWithDelay(Sound.sound()
                                                                                .source(Sound.Source.UI)
                                                                                .type(org.bukkit.Sound.BLOCK_LAVA_EXTINGUISH)
                                                                                .pitch(0.65f)
                                                                                .volume(3.0f).build(), 200)
                                                                .withPlayerAsSource()
                                                                .build())
                                                .addReloadEffect(
                                                                SoundEffect.builder().addSound(
                                                                                Sound.sound()
                                                                                                .source(Sound.Source.UI)
                                                                                                .type(org.bukkit.Sound.BLOCK_LAVA_EXTINGUISH)
                                                                                                .pitch(0.65f)
                                                                                                .volume(3.0f).build())
                                                                                .build())
                                                .build())
                                .withSecondaryAction(
                                                WeaponAction.builder().withReloadTime(10)
                                                                .withProjectileMaxTicksLived(10)
                                                                .withVelocityMultiplier(1.5f)
                                                                .withProjectileMaterial(Material.TURTLE_EGG)
                                                                .addEffect(new ExplosionEffect(2.4f))
                                                                .addEffect(new KnockbackEffect(1.1f))
                                                                .addEffect(SoundEffect.explosionSound())
                                                                .addEffect(GeometricVisualEffect.builder()
                                                                                .withParticleBuilder(
                                                                                                Particle.FLAME
                                                                                                                .builder())
                                                                                .withParticleCount(200)
                                                                                .withShape(GeometricVisualEffect.Shape.Sphere
                                                                                                .builder()
                                                                                                .withOffset(new GeometricVisualEffect.Offset.FromPoint()
                                                                                                                .withSpeed(0.1f))
                                                                                                .withRadius(.25f)
                                                                                                .build())
                                                                                .build())
                                                                .addEffect(GeometricVisualEffect.builder()
                                                                                .withParticleBuilder(
                                                                                                Particle.SMOKE
                                                                                                                .builder())
                                                                                .withParticleCount(200)
                                                                                .withShape(GeometricVisualEffect.Shape.Sphere
                                                                                                .builder()
                                                                                                .withOffset(new GeometricVisualEffect.Offset.FromPoint()
                                                                                                                .withSpeed(2.f))
                                                                                                .withRadius(.25f)
                                                                                                .build())
                                                                                .build())
                                                                .addEffect(GeometricVisualEffect.builder()
                                                                                .withParticleBuilder(
                                                                                                Particle.LARGE_SMOKE
                                                                                                                .builder())
                                                                                .withParticleCount(150)
                                                                                .withShape(GeometricVisualEffect.Shape.Sphere
                                                                                                .builder()
                                                                                                .withOffset(new GeometricVisualEffect.Offset.FromPoint()
                                                                                                                .withSpeed(0.2f))
                                                                                                .withRadius(.5f)
                                                                                                .build())
                                                                                .build())
                                                                .addTrailEffect(VisualEffect.builder()
                                                                                .addParticle(Particle.LARGE_SMOKE
                                                                                                .builder()
                                                                                                .offset(0, 0, 0)
                                                                                                .extra(0))
                                                                                .build())
                                                                .build())

                                .withSneakAction(WeaponAction.builder().withReloadTime(450)
                                                .addCastEffect(new SelfPotionEffect(Arrays.asList(new PotionEffect(
                                                                PotionEffectType.SPEED, 300,
                                                                2))))
                                                .addCastEffect(SoundEffect.builder().addSound(
                                                                Sound.sound()
                                                                                .source(Sound.Source.HOSTILE)
                                                                                .type(org.bukkit.Sound.ENTITY_IRON_GOLEM_REPAIR)
                                                                                .volume(1.5f).build())
                                                                .withPlayerAsSource()
                                                                .build())
                                                .addReloadEffect(SoundEffect.builder().addSound(
                                                                Sound.sound()
                                                                                .source(Sound.Source.UI)
                                                                                .type(org.bukkit.Sound.ENTITY_IRON_GOLEM_STEP)
                                                                                .volume(1.0f).build())
                                                                .withPitchRange(0.8f, 1.2f)
                                                                .withPlayerAsSource()
                                                                .build())
                                                .build())
                                .build());

                WeaponEffect breezyDashEffect = GeometricVisualEffect.builder()
                                .withParticleBuilder(Particle.GUST.builder())
                                .withShape(GeometricVisualEffect.Shape.Ring.builder()
                                                .withRadius(0.75f)
                                                .withOffset(new GeometricVisualEffect.Offset.InDirectionRelative(
                                                                new Vector(0, 0, -1))
                                                                .withSpeed(1.2f))
                                                .build())
                                .withPlayerAsSource()
                                .withParticleCount(15)
                                .build();

                register(Weapon.builder("breezy_hoe")
                                .withDisplayName(
                                                MiniMessage.miniMessage().deserialize(
                                                                "<gradient:#1C273B:#6C6699>Breezy Hoe</gradient>"))
                                .withWeaponItemMaterial(Material.BREEZE_ROD)
                                .withPrimaryAction(WeaponAction.builder().withReloadTime(25)
                                                .addCastEffect(new DashEffect(1.8f))
                                                .addCastEffect(breezyDashEffect)
                                                .addCastEffect(new DelayEffect(8, Arrays.asList(breezyDashEffect)))
                                                .addCastEffect(SoundEffect.builder().addSound(
                                                                Sound.sound()
                                                                                .source(Sound.Source.HOSTILE)
                                                                                .type(org.bukkit.Sound.ENTITY_BREEZE_JUMP)
                                                                                .volume(2.0f).build())
                                                                .withPlayerAsSource()
                                                                .build())
                                                .build())
                                .withSecondaryAction(
                                                WeaponAction.builder().withReloadTime(14)
                                                                .withProjectile(WindCharge.class)
                                                                .withVelocityMultiplier(1.25f)
                                                                .addCastEffect(
                                                                                SoundEffect.builder().addSound(
                                                                                                Sound.sound()
                                                                                                                .source(Sound.Source.HOSTILE)
                                                                                                                .type(org.bukkit.Sound.ENTITY_BREEZE_SHOOT)
                                                                                                                .volume(1.5f)
                                                                                                                .build())
                                                                                                .withPlayerAsSource()
                                                                                                .build())
                                                                .addEffect(new ExplosionEffect(2.6f))
                                                                .addEffect(new KnockbackEffect(1.6f))

                                                                .addEffect(VisualEffect.builder()
                                                                                .addParticle(Particle.GUST_EMITTER_SMALL
                                                                                                .builder())
                                                                                .build())
                                                                .addEffect(SoundEffect.builder().addSound(
                                                                                Sound.sound()
                                                                                                .source(Sound.Source.HOSTILE)
                                                                                                .type(org.bukkit.Sound.ENTITY_WIND_CHARGE_WIND_BURST)
                                                                                                .volume(3.0f).build())
                                                                                .build())
                                                                .addTrailEffect(VisualEffect.builder()
                                                                                .addParticle(Particle.CLOUD.builder()
                                                                                                .offset(0, 0, 0)
                                                                                                .extra(0))
                                                                                .build())
                                                                .withTrailEffectDelay(5)
                                                                .withTrailEffectTimer(3)
                                                                .addEffect(new DamageEffect(3.0f, 20.f,
                                                                                DamageType.PLAYER_EXPLOSION, false))
                                                                .build())
                                .withSneakAction(WeaponAction.builder().withReloadTime(300)
                                                .addCastEffect(new SelfPotionEffect(Arrays.asList(new PotionEffect(
                                                                PotionEffectType.SPEED, 50, 3)))
                                                                .withHiddenSplashEffect())
                                                .addCastEffect(SoundEffect.builder().addSound(
                                                                Sound.sound()
                                                                                .source(Sound.Source.HOSTILE)
                                                                                .type(org.bukkit.Sound.ENTITY_BREEZE_CHARGE)
                                                                                .volume(1.0f).build())
                                                                .withPlayerAsSource()
                                                                .build())
                                                .addReloadEffect(SoundEffect.builder().addSound(
                                                                Sound.sound()
                                                                                .source(Sound.Source.UI)
                                                                                .type(org.bukkit.Sound.ENTITY_BREEZE_DEFLECT)
                                                                                .volume(1.0f).build())
                                                                .build())
                                                .build())
                                .build());

                WeaponEffect blazingDashEffect = GeometricVisualEffect.builder()
                                .withParticleBuilder(Particle.LARGE_SMOKE.builder())
                                .withShape(GeometricVisualEffect.Shape.Ring.builder()
                                                .withRadius(0.75f)
                                                .withOffset(new GeometricVisualEffect.Offset.InDirectionRelative(
                                                                new Vector(0, 0, -1))
                                                                .withSpeed(1.2f))
                                                .build())
                                .withParticleCount(500)
                                .withPlayerAsSource()
                                .build();

                Sound blazeShootSound = Sound.sound()
                                .source(Sound.Source.HOSTILE)
                                .type(org.bukkit.Sound.ENTITY_BLAZE_SHOOT)
                                .volume(1.5f)
                                .build();
                register(Weapon.builder("blazing_hoe")
                                .withDisplayName(
                                                MiniMessage.miniMessage().deserialize(
                                                                "<gradient:#4F2400:#FCFC82>Blazing Hoe</gradient>"))
                                .withWeaponItemMaterial(Material.BLAZE_ROD)
                                .withPrimaryAction(WeaponAction.builder().withReloadTime(25)
                                                .addCastEffect(new DashEffect(1.2f))
                                                .addCastEffect(blazingDashEffect)
                                                .addCastEffect(new DelayEffect(8, Arrays.asList(blazingDashEffect)))
                                                .addCastEffect(SoundEffect.builder().addSound(
                                                                Sound.sound()
                                                                                .source(Sound.Source.HOSTILE)
                                                                                .type(org.bukkit.Sound.ENTITY_BLAZE_AMBIENT)
                                                                                .volume(2.0f).build())
                                                                .withPlayerAsSource()
                                                                .build())
                                                .build())
                                .withSecondaryAction(
                                                WeaponAction.builder().withReloadTime(38)
                                                                .withProjectile(SmallFireball.class)
                                                                .withVelocityMultiplier(1.25f)
                                                                .withBurstCount(3)
                                                                .withBurstDelayTicks(6)
                                                                .addCastEffect(
                                                                                SoundEffect.builder().addSound(
                                                                                                blazeShootSound)
                                                                                                .addSoundWithDelay(
                                                                                                                blazeShootSound,
                                                                                                                6)
                                                                                                .addSoundWithDelay(
                                                                                                                blazeShootSound,
                                                                                                                12)
                                                                                                .withPlayerAsSource()
                                                                                                .build())
                                                                .addEffect(new ExplosionEffect(1.25f))
                                                                .addEffect(new KnockbackEffect(1.6f))
                                                                .addEffect(VisualEffect.builder()
                                                                                .addParticle(Particle.LARGE_SMOKE
                                                                                                .builder().count(100)
                                                                                                .offset(0, -1, 0)
                                                                                                .extra(0.2))
                                                                                .build())
                                                                .addEffect(SoundEffect.builder().addSound(
                                                                                Sound.sound()
                                                                                                .source(Sound.Source.HOSTILE)
                                                                                                .type(org.bukkit.Sound.ENTITY_BLAZE_BURN)
                                                                                                .volume(3.0f).build())
                                                                                .build())
                                                                .addEffect(new DamageEffect(3.0f, 20.f,
                                                                                DamageType.PLAYER_EXPLOSION, false))
                                                                .build())
                                .withSneakAction(WeaponAction.builder().withReloadTime(480)
                                                .addCastEffect(new SelfPotionEffect(Arrays.asList(new PotionEffect(
                                                                PotionEffectType.LEVITATION, 50, 3)))
                                                                .withHiddenSplashEffect())
                                                .addCastEffect(new DelayEffect(50, Arrays.asList(
                                                                new SelfPotionEffect(Arrays.asList(new PotionEffect(
                                                                                PotionEffectType.SLOW_FALLING, 100, 3)))
                                                                                .withHiddenSplashEffect())))
                                                .addCastEffect(SoundEffect.builder().addSound(
                                                                Sound.sound()
                                                                                .source(Sound.Source.HOSTILE)
                                                                                .type(org.bukkit.Sound.ENTITY_BLAZE_BURN)
                                                                                .volume(1.0f).build())
                                                                .build())
                                                .addReloadEffect(SoundEffect.builder().addSound(
                                                                Sound.sound()
                                                                                .source(Sound.Source.UI)
                                                                                .type(org.bukkit.Sound.ENTITY_BLAZE_HURT)
                                                                                .volume(1.0f).build())
                                                                .build())
                                                .build())
                                .build());

                WeaponEffect dashGeometricEffect = GeometricVisualEffect.builder()
                                .withParticleBuilder(Particle.POOF.builder())
                                .withShape(GeometricVisualEffect.Shape.Ring.builder()
                                                .withRadius(0.5f)
                                                .withOffset(new GeometricVisualEffect.Offset.InDirectionRelative(
                                                                new Vector(0, 0, -1))
                                                                .withSpeed(1.2f))
                                                .build())
                                .withParticleCount(100)
                                .withPlayerAsSource()
                                .build();

                register(Weapon.builder("wanderer_hoe")
                                .withDisplayName(
                                                MiniMessage.miniMessage().deserialize(
                                                                "<gradient:#2C456C:#435F91>Wanderer's</gradient> <gradient:#F2C039:#CC8E29>Hoe</gradient>"))
                                .withPrimaryAction(WeaponAction.builder().withReloadTime(100)
                                                .withVelocityMultiplier(4.8f)
                                                .addEffect(new KnockbackEffect(1.2f))
                                                .withProjectileMaterial(Material.EMERALD)
                                                .addCastEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.UI)
                                                                .type(org.bukkit.Sound.ENTITY_WANDERING_TRADER_YES)
                                                                .volume(3.0f).build()).withPlayerAsSource().build())
                                                .addCastEffect(GeometricVisualEffect.builder()
                                                                .withParticleBuilder(Particle.ITEM.builder().data(
                                                                                ItemType.EMERALD.createItemStack()))
                                                                .withShape(GeometricVisualEffect.Shape.Ring.builder()
                                                                                .withRadius(0.5f)
                                                                                .withOffset(new GeometricVisualEffect.Offset.InDirectionRelative(
                                                                                                new Vector(0, 0, 1))
                                                                                                .withSpeed(0.5f))
                                                                                .build())
                                                                .withParticleCount(25)
                                                                .withPlayerAsSource()
                                                                .build())
                                                .addEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.HOSTILE)
                                                                .type(org.bukkit.Sound.ENTITY_WIND_CHARGE_WIND_BURST)
                                                                .volume(3.0f).build()).build())
                                                .addEffect(VisualEffect.builder()
                                                                .addParticle(Particle.GUST_EMITTER_SMALL.builder())
                                                                .build())
                                                .addTrailEffect(VisualEffect.builder()
                                                                .addParticle(Particle.ITEM.builder()
                                                                                .data(ItemType.EMERALD
                                                                                                .createItemStack())
                                                                                .offset(0, 0, 0).extra(0))
                                                                .build())
                                                .build())
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(14)
                                                .withVelocityMultiplier(4.8f)
                                                .addCastEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.UI)
                                                                .type(org.bukkit.Sound.ENTITY_LLAMA_SPIT)
                                                                .volume(3.0f).build()).withPlayerAsSource().build())
                                                .addEffect(new ExplosionEffect(2.6f))
                                                .addEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.HOSTILE)
                                                                .type(org.bukkit.Sound.BLOCK_SLIME_BLOCK_BREAK)
                                                                .volume(3.5f).pitch(0.5f).build())
                                                                .withPitchRange(0.4f, 0.6f).build())
                                                .addEffect(new KnockbackEffect(1.1f))
                                                .addEffect(GeometricVisualEffect.builder()
                                                                .withParticleBuilder(Particle.SPIT.builder())
                                                                .withParticleCount(150)
                                                                .withShape(GeometricVisualEffect.Shape.Sphere.builder()
                                                                                .withOffset(new GeometricVisualEffect.Offset.FromPoint()
                                                                                                .withSpeed(0.5f))
                                                                                .withRadius(1.f).build())
                                                                .build())
                                                .addTrailEffect(VisualEffect.builder()
                                                                .addParticle(Particle.SPIT.builder().offset(0, 0, 0)
                                                                                .extra(0))
                                                                .build())
                                                .withProjectile(LlamaSpit.class)
                                                .build())
                                .withSneakAction(WeaponAction.builder().withReloadTime(320)
                                                .withVelocityMultiplier(4.8f)
                                                .addCastEffect(new DashEffect(0.9f))
                                                .addCastEffect(dashGeometricEffect)
                                                .addCastEffect(new DelayEffect(8, Arrays.asList(dashGeometricEffect)))
                                                .addCastEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.HOSTILE)
                                                                .type(org.bukkit.Sound.ENTITY_WANDERING_TRADER_REAPPEARED)
                                                                .volume(3.0f).build()).withPlayerAsSource().build())
                                                .addReloadEffect(SoundEffect.builder().addSound(
                                                                Sound.sound()
                                                                                .source(Sound.Source.UI)
                                                                                .type(org.bukkit.Sound.ENTITY_WANDERING_TRADER_TRADE)
                                                                                .volume(1.0f).build())
                                                                .build())
                                                .build())

                                .build());

                register(Weapon.builder("the_eye")
                                .withDisplayName(
                                                MiniMessage.miniMessage().deserialize(
                                                                "<gradient:#316364:#4E8386>The</gradient> <gradient:#B5E45A:#579143>Eye</gradient>"))
                                .withWeaponItemMaterial(Material.GRASS_BLOCK)
                                .withPrimaryAction(
                                                WeaponAction.builder().withReloadTime(100).withVelocityMultiplier(2.8f)
                                                                .addEffect(GeometricVisualEffect.builder()
                                                                                .withParticleBuilder(
                                                                                                Particle.PORTAL
                                                                                                                .builder())
                                                                                .withShape(GeometricVisualEffect.Shape.Sphere
                                                                                                .builder()
                                                                                                .withRadius(1.5f)
                                                                                                .withOffset(new GeometricVisualEffect.Offset.FromPoint(
                                                                                                                false)
                                                                                                                .withSpeed(1.5f))
                                                                                                .build())
                                                                                .withPlayerAsSource()
                                                                                .build())
                                                                .addEffect(new TeleportEffect())
                                                                .addEffect(SoundEffect.builder().addSound(Sound
                                                                                .sound()
                                                                                .source(Sound.Source.HOSTILE)
                                                                                .type(org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT)
                                                                                .volume(3.0f).build())
                                                                                .withPlayerAsSource().build())
                                                                .withProjectileMaxTicksLived(8)
                                                                .addCastEffect(SoundEffect.builder().addSound(Sound
                                                                                .sound()
                                                                                .source(Sound.Source.HOSTILE)
                                                                                .type(org.bukkit.Sound.ENTITY_ENDER_PEARL_THROW)
                                                                                .volume(3.0f).build())
                                                                                .withPlayerAsSource().build())
                                                                .addCastEffect(GeometricVisualEffect.builder()
                                                                                .withParticleBuilder(Particle.ITEM
                                                                                                .builder().data(
                                                                                                                ItemType.ENDER_PEARL
                                                                                                                                .createItemStack()))
                                                                                .withShape(GeometricVisualEffect.Shape.Ring
                                                                                                .builder()
                                                                                                .withRadius(0.85f)
                                                                                                .withOffset(new GeometricVisualEffect.Offset.InDirectionRelative(
                                                                                                                new Vector(0, 0, 1))
                                                                                                                .withSpeed(0.75f))
                                                                                                .build())
                                                                                .withParticleCount(25)
                                                                                .withPlayerAsSource()
                                                                                .build())
                                                                .addTrailEffect(VisualEffect.builder()
                                                                                .addParticle(Particle.ITEM.builder()
                                                                                                .data(ItemType.ENDER_PEARL
                                                                                                                .createItemStack())
                                                                                                .offset(0, 0, 0)
                                                                                                .extra(0))
                                                                                .build())
                                                                .withProjectileMaterial(Material.ENDER_PEARL)
                                                                .build())
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(18)
                                                .withVelocityMultiplier(4.8f)
                                                .addEffect(new DelayEffect(10,
                                                                Arrays.asList(new ExplosionEffect(2.6f))))
                                                .addEffect(new KnockbackEffect(1.1f))
                                                .addEffect(GeometricVisualEffect.builder()
                                                                .withParticleBuilder(Particle.REVERSE_PORTAL.builder())
                                                                .withShape(GeometricVisualEffect.Shape.Sphere.builder()
                                                                                .withRadius(2.5f)
                                                                                .withOffset(new GeometricVisualEffect.Offset.FromPoint(
                                                                                                false)
                                                                                                .withSpeed(1.5f))
                                                                                .build())
                                                                .build())
                                                .addEffect(
                                                                SoundEffect.builder().addSound(Sound
                                                                                .sound()
                                                                                .source(Sound.Source.HOSTILE)
                                                                                .type(org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT)
                                                                                .pitch(0.5f)
                                                                                .volume(3.0f).build())
                                                                                .build())
                                                .withProjectileMaterial(Material.ENDER_EYE)
                                                .addCastEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.HOSTILE)
                                                                .type(org.bukkit.Sound.ENTITY_ENDERMAN_HURT)
                                                                .volume(3.0f).build()).withPlayerAsSource().build())
                                                .addCastEffect(GeometricVisualEffect.builder()
                                                                .withParticleBuilder(Particle.ITEM
                                                                                .builder().data(
                                                                                                ItemType.ENDER_EYE
                                                                                                                .createItemStack()))
                                                                .withShape(GeometricVisualEffect.Shape.Ring
                                                                                .builder()
                                                                                .withRadius(0.85f)
                                                                                .withOffset(new GeometricVisualEffect.Offset.InDirectionRelative(
                                                                                                new Vector(0, 0, 1))
                                                                                                .withSpeed(0.75f))
                                                                                .build())
                                                                .withParticleCount(25)
                                                                .withPlayerAsSource()
                                                                .build())
                                                .addTrailEffect(VisualEffect.builder()
                                                                .addParticle(Particle.ITEM.builder()
                                                                                .data(ItemType.ENDER_EYE
                                                                                                .createItemStack())
                                                                                .offset(0, 0, 0).extra(0))
                                                                .build())
                                                .build())
                                .withSneakAction(WeaponAction.builder().withReloadTime(450)
                                                .addCastEffect(new SelfPotionEffect(Arrays.asList(new PotionEffect(
                                                                PotionEffectType.SPEED, 300,
                                                                2))).withHiddenSplashEffect())
                                                .addCastEffect(SoundEffect.builder().addSound(
                                                                Sound.sound()
                                                                                .source(Sound.Source.HOSTILE)
                                                                                .type(org.bukkit.Sound.ENTITY_ENDERMAN_STARE)
                                                                                .volume(1.5f).build())
                                                                .withPlayerAsSource()
                                                                .build())
                                                .addReloadEffect(SoundEffect.builder().addSound(
                                                                Sound.sound()
                                                                                .source(Sound.Source.UI)
                                                                                .type(org.bukkit.Sound.ENTITY_ENDERMAN_SCREAM)
                                                                                .volume(1.0f).build())
                                                                .withPitchRange(0.8f, 1.2f)
                                                                .withPlayerAsSource()
                                                                .build())
                                                .build())
                                .build());

                WeaponEffect allayDashEffect = GeometricVisualEffect.builder()
                                .withParticleBuilder(Particle.BUBBLE_POP.builder())
                                .withShape(GeometricVisualEffect.Shape.Ring.builder()
                                                .withRadius(0.75f)
                                                .withOffset(new GeometricVisualEffect.Offset.InDirectionRelative(
                                                                new Vector(0, 0, -1))
                                                                .withSpeed(1.2f))
                                                .build())
                                .withParticleCount(500)
                                .withPlayerAsSource()
                                .build();

                register(Weapon.builder("allay_hoe")
                                .withDisplayName(
                                                MiniMessage.miniMessage().deserialize(
                                                                "<gradient:#FFFFFF:#6CC2F2>Allay Hoe</gradient>"))
                                .withWeaponItemMaterial(Material.AMETHYST_SHARD)
                                .withPrimaryAction(WeaponAction.builder().withReloadTime(25)
                                                .addCastEffect(new DashEffect(1.2f))
                                                .addCastEffect(allayDashEffect)
                                                .addCastEffect(new DelayEffect(8, Arrays.asList(allayDashEffect)))
                                                .addCastEffect(SoundEffect.builder().addSound(
                                                                Sound.sound()
                                                                                .source(Sound.Source.HOSTILE)
                                                                                .type(org.bukkit.Sound.ENTITY_ALLAY_ITEM_THROWN)
                                                                                .volume(2.0f).build())
                                                                .withPitchRange(0.5f, 4f)
                                                                .withPlayerAsSource()
                                                                .build())
                                                .build())
                                .withSneakAction(WeaponAction.builder().withReloadTime(100)
                                                .withVelocityMultiplier(2.8f)
                                                .addCastEffect(new SplashPotionEffect(
                                                                Arrays.asList(new PotionEffect(
                                                                                PotionEffectType.REGENERATION, 5, 4)),
                                                                5)
                                                                .affectsEnemyTeam(false).affectsOwnTeam(true))
                                                .addCastEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.HOSTILE)
                                                                .type(org.bukkit.Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM)
                                                                .volume(3.0f).build()).withPlayerAsSource().build())
                                                .build())
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(14)
                                                .withVelocityMultiplier(4.8f)
                                                .withProjectiles(2)
                                                .addEffect(new ExplosionEffect(2.6f))
                                                .addEffect(new KnockbackEffect(1.1f))
                                                .addEffect(SoundEffect.explosionSound())
                                                .withProjectileMaterial(Material.AMETHYST_SHARD)
                                                .addCastEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.HOSTILE)
                                                                .type(org.bukkit.Sound.ENTITY_ALLAY_ITEM_GIVEN)
                                                                .volume(3.0f).build()).withPlayerAsSource().build())
                                                .addEffect(VisualEffect.builder()
                                                                .addParticle(Particle.FLASH.builder()
                                                                                .color(Color.fromRGB(133, 49, 141)))
                                                                .build())
                                                .addEffect(GeometricVisualEffect.builder()
                                                                .withParticleBuilder(
                                                                                Particle.ITEM
                                                                                                .builder()
                                                                                                .data(ItemType.AMETHYST_SHARD
                                                                                                                .createItemStack()))
                                                                .withParticleCount(200)
                                                                .withShape(GeometricVisualEffect.Shape.Sphere
                                                                                .builder()
                                                                                .withOffset(new GeometricVisualEffect.Offset.FromPoint()
                                                                                                .withSpeed(2.f))
                                                                                .withRadius(.25f)
                                                                                .build())
                                                                .build())
                                                .addTrailEffect(VisualEffect.builder()
                                                                .addParticle(Particle.ITEM.builder()
                                                                                .data(ItemType.AMETHYST_SHARD
                                                                                                .createItemStack())
                                                                                .offset(0, 0, 0).extra(0))
                                                                .build())
                                                .build())
                                .build());

                register(Weapon.builder("pillager_hoe")
                                .withDisplayName(
                                                MiniMessage.miniMessage().deserialize(
                                                                "<gradient:#FFFFFF:#593210>Pillager Hoe</gradient>"))
                                .withWeaponItemMaterial(Material.CROSSBOW)
                                .withPrimaryAction(WeaponAction.builder().withReloadTime(25)
                                                .addCastEffect(new DashEffect(1.2f))
                                                .addCastEffect(SoundEffect.builder().addSound(
                                                                Sound.sound()
                                                                                .source(Sound.Source.HOSTILE)
                                                                                .type(org.bukkit.Sound.ENTITY_PILLAGER_HURT)
                                                                                .volume(2.0f).build())
                                                                .withPitchRange(0.8f, 1.2f)
                                                                .withPlayerAsSource()
                                                                .build())
                                                .addCastEffect(GeometricVisualEffect.builder()
                                                                .withParticleBuilder(Particle.ITEM.builder().data(
                                                                                ItemType.CROSSBOW
                                                                                                .createItemStack()))
                                                                .withShape(GeometricVisualEffect.Shape.Ring.builder()
                                                                                .withRadius(0.5f)
                                                                                .withOffset(new GeometricVisualEffect.Offset.InDirectionRelative(
                                                                                                new Vector(0, 0, 1))
                                                                                                .withSpeed(0.5f))
                                                                                .build())
                                                                .withParticleCount(25)
                                                                .withPlayerAsSource()
                                                                .build())
                                                .build())
                                .withSneakAction(WeaponAction.builder().withReloadTime(100)
                                                .withVelocityMultiplier(2.8f)
                                                .addCastEffect(new SplashPotionEffect(
                                                                Arrays.asList(new PotionEffect(
                                                                                PotionEffectType.REGENERATION, 5, 4)),
                                                                5)
                                                                .affectsEnemyTeam(false).affectsOwnTeam(true))
                                                .addCastEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.HOSTILE)
                                                                .type(org.bukkit.Sound.ENTITY_PILLAGER_CELEBRATE)
                                                                .volume(3.0f).build())
                                                                .withPitchRange(0.8f, 1.2f)
                                                                .withPlayerAsSource().build())
                                                .build())
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(14)
                                                .withProjectile(Firework.class)
                                                .withVelocityMultiplier(1.4f)
                                                .withProjectiles(3)
                                                .withProjectileMaxTicksLived(6)
                                                .addEffect(new ExplosionEffect(1.5f))
                                                .addEffect(VisualEffect.builder()
                                                                .addParticle(Particle.FLASH.builder()
                                                                                .color(Color.fromRGB(255, 255, 255)))
                                                                .build())
                                                .addEffect(new KnockbackEffect(1.1f))
                                                .addCastEffect(new DashEffect(-0.8f))
                                                .addEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.UI)
                                                                .type(org.bukkit.Sound.ENTITY_FIREWORK_ROCKET_BLAST)
                                                                .volume(3.0f).build()).build())
                                                .addEffect(GeometricVisualEffect.builder()
                                                                .withParticleBuilder(
                                                                                Particle.FIREWORK
                                                                                                .builder())
                                                                .withParticleCount(200)
                                                                .withShape(GeometricVisualEffect.Shape.Sphere
                                                                                .builder()
                                                                                .withOffset(new GeometricVisualEffect.Offset.FromPoint()
                                                                                                .withSpeed(2.f))
                                                                                .withRadius(.25f)
                                                                                .build())
                                                                .build())
                                                .withProjectileMaterial(Material.FIREWORK_ROCKET)
                                                .addCastEffect(SoundEffect.builder().addSound(Sound.sound()
                                                                .source(Sound.Source.UI)
                                                                .type(org.bukkit.Sound.ENTITY_FIREWORK_ROCKET_SHOOT)
                                                                .volume(3.0f).build())
                                                                .addSound(Sound.sound()
                                                                                .source(Sound.Source.HOSTILE)
                                                                                .type(org.bukkit.Sound.ENTITY_PILLAGER_AMBIENT)
                                                                                .volume(3.0f).build())
                                                                .withPitchRange(0.8f, 1.2f)
                                                                .withPlayerAsSource().build())
                                                .build())
                                .build());

                // TODO: These Projectiles probably explode on their own, should this be
                // cancelled and managed by ourselves? How to clone the "charged effect"?
                register(Weapon.builder("withered_hoe")
                                .withDisplayName(MiniMessage.miniMessage()
                                                .deserialize("<gradient:#221a17:#42352d>Withered Hoe</gradient>"))
                                .withWeaponItemMaterial(Material.NETHERITE_HOE)
                                .withSneakAction(WeaponAction.builder()
                                                .withReloadTime(300)
                                                .addCastEffect(SoundEffect.builder()
                                                                .addSound(Sound.sound().source(Sound.Source.UI).type(
                                                                                org.bukkit.Sound.ENTITY_GENERIC_EXPLODE)
                                                                                .build())
                                                                .withPitchRange(0.56f, 0.84f)
                                                                .withPlayerAsSource().build())
                                                .addCastEffect(SoundEffect.builder()
                                                                .addSound(Sound.sound().source(Sound.Source.UI).type(
                                                                                org.bukkit.Sound.ENTITY_WITHER_SPAWN)
                                                                                .build())
                                                                .withPlayerAsSource().build())
                                                .addCastEffect(new ExplosionEffect(4.5f))
                                                .addCastEffect(GeometricVisualEffect.builder()
                                                                .withParticleBuilder(
                                                                                Particle.SMOKE.builder())
                                                                .withParticleCount(650)
                                                                .withShape(GeometricVisualEffect.Shape.Sphere.builder()
                                                                                .withOffset(new GeometricVisualEffect.Offset.FromPoint()
                                                                                                .withSpeed(8.2f))
                                                                                .withRadius(.1f).build())
                                                                .build())
                                                .addCastEffect(new SplashPotionEffect(Arrays
                                                                .asList(PotionEffectType.WITHER.createEffect(40, 1)), 8)
                                                                .withHiddenSplashEffect())
                                                .build())
                                .withPrimaryAction(WeaponAction.builder().withProjectile(WitherSkull.class)
                                                .withVelocityMultiplier(1.7f)
                                                .withReloadTime(50)
                                                .addCastEffect(SoundEffect.builder()
                                                                .addSound(Sound.sound().source(Sound.Source.UI).type(
                                                                                org.bukkit.Sound.ENTITY_WITHER_SHOOT)
                                                                                .build())
                                                                .withPitchRange(0.8f, 1.2f)
                                                                .withPlayerAsSource().build())
                                                .addEffect(new ExplosionEffect(1.7f))
                                                .addEffect(new SplashPotionEffect(Arrays
                                                                .asList(PotionEffectType.WITHER.createEffect(40, 2)), 8)
                                                                .withHiddenSplashEffect())
                                                .addEffect(SoundEffect.builder()
                                                                .addSound(Sound.sound().source(Sound.Source.HOSTILE)
                                                                                .type(
                                                                                                org.bukkit.Sound.ENTITY_WITHER_BREAK_BLOCK)
                                                                                .volume(3.0f)
                                                                                .build())
                                                                .withPitchRange(0.8f, 1.2f)
                                                                .withPlayerAsSource().build())
                                                .isCharged(true)
                                                .addEffect(GeometricVisualEffect.builder()
                                                                .withParticleBuilder(
                                                                                Particle.SMOKE.builder())
                                                                .withShape(GeometricVisualEffect.Shape.Sphere.builder()
                                                                                .withOffset(new GeometricVisualEffect.Offset.FromPoint()
                                                                                                .withSpeed(1.0f))
                                                                                .withRadius(.25f).build())
                                                                .build())
                                                .build())
                                .withSecondaryAction(WeaponAction.builder().withProjectile(WitherSkull.class)
                                                .withVelocityMultiplier(2.7f)
                                                .withReloadTime(20)
                                                .addCastEffect(SoundEffect.builder()
                                                                .addSound(Sound.sound().source(Sound.Source.UI).type(
                                                                                org.bukkit.Sound.ENTITY_WITHER_SHOOT)
                                                                                .build())
                                                                .withPitchRange(0.8f, 1.2f)
                                                                .withPlayerAsSource().build())
                                                .addEffect(new ExplosionEffect(1.7f))
                                                .addEffect(new SplashPotionEffect(Arrays
                                                                .asList(PotionEffectType.WITHER.createEffect(20, 1)), 6)
                                                                .withHiddenSplashEffect())
                                                .addEffect(SoundEffect.builder()
                                                                .addSound(Sound.sound().source(Sound.Source.HOSTILE)
                                                                                .type(
                                                                                                org.bukkit.Sound.ENTITY_WITHER_BREAK_BLOCK)
                                                                                .volume(3.0f)
                                                                                .build())
                                                                .withPitchRange(0.8f, 1.2f)
                                                                .withPlayerAsSource().build())
                                                .addEffect(GeometricVisualEffect.builder()
                                                                .withParticleBuilder(
                                                                                Particle.SMOKE.builder())
                                                                .withShape(GeometricVisualEffect.Shape.Sphere.builder()
                                                                                .withOffset(new GeometricVisualEffect.Offset.FromPoint()
                                                                                                .withSpeed(1.0f))
                                                                                .withRadius(.25f).build())
                                                                .build())
                                                .build())
                                .build());

                WeaponEffect slimyDashEffect = GeometricVisualEffect.builder()
                                .withParticleBuilder(Particle.ITEM_SLIME.builder())
                                .withShape(GeometricVisualEffect.Shape.Ring.builder()
                                                .withRadius(0.75f)
                                                .withOffset(new GeometricVisualEffect.Offset.InDirectionRelative(
                                                                new Vector(0, 0, -1))
                                                                .withSpeed(1.2f))
                                                .build())
                                .withParticleCount(500)
                                .withPlayerAsSource()
                                .build();

                register(Weapon.builder("slimy_hoe").withWeaponItemMaterial(Material.SLIME_BLOCK)
                                .withDisplayName(MiniMessage.miniMessage()
                                                .deserialize("<gradient:#6AA84F:#223622>Slimy Hoe</gradient>"))
                                .withPrimaryAction(WeaponAction.builder().withReloadTime(450)
                                                .addCastEffect(new SelfPotionEffect(Arrays.asList(new PotionEffect(
                                                                PotionEffectType.JUMP_BOOST, 300,
                                                                3))))
                                                .addCastEffect(SoundEffect.builder().addSound(
                                                                Sound.sound()
                                                                                .source(Sound.Source.HOSTILE)
                                                                                .type(org.bukkit.Sound.ENTITY_SLIME_HURT)
                                                                                .volume(1.5f).build())
                                                                .withPlayerAsSource()
                                                                .build())
                                                .addReloadEffect(SoundEffect.builder().addSound(
                                                                Sound.sound()
                                                                                .source(Sound.Source.UI)
                                                                                .type(org.bukkit.Sound.ENTITY_SLIME_DEATH)
                                                                                .volume(1.0f).build())
                                                                .withPitchRange(0.8f, 1.2f)
                                                                .withPlayerAsSource()
                                                                .build())
                                                .build())
                                .withSneakAction(WeaponAction.builder().withReloadTime(25)
                                                .addCastEffect(new DashEffect(1.2f))
                                                .addCastEffect(slimyDashEffect)
                                                .addCastEffect(new DelayEffect(8, Arrays.asList(slimyDashEffect)))
                                                .addCastEffect(SoundEffect.builder().addSound(
                                                                Sound.sound()
                                                                                .source(Sound.Source.UI)
                                                                                .type(org.bukkit.Sound.ENTITY_SLIME_JUMP)
                                                                                .volume(2.0f).build())
                                                                .withPlayerAsSource()
                                                                .build())
                                                .build())
                                .withSecondaryAction(WeaponAction.builder().withReloadTime(35)
                                                .withVelocityMultiplier(4.2f)
                                                .withProjectileSplittingCount(3)
                                                .withMaxProjectileSplits(1)
                                                .addCastEffect(SoundEffect.builder()
                                                                .addSound(Sound.sound().source(Sound.Source.UI)
                                                                                .type(
                                                                                                org.bukkit.Sound.ENTITY_SNOWBALL_THROW)
                                                                                .volume(3.0f)
                                                                                .build())
                                                                .withPitchRange(0.8f, 1.2f)
                                                                .withPlayerAsSource().build())
                                                .addEffect(new ExplosionEffect(2.5f))
                                                .addEffect(GeometricVisualEffect.builder()
                                                                .withParticleBuilder(
                                                                                Particle.BLOCK.builder().data(
                                                                                                BlockType.SLIME_BLOCK
                                                                                                                .createBlockData()))
                                                                .withShape(GeometricVisualEffect.Shape.Sphere.builder()
                                                                                .withOffset(new GeometricVisualEffect.Offset.FromPoint()
                                                                                                .withSpeed(1.0f))
                                                                                .withRadius(.5f).build())
                                                                .build())
                                                .addEffect(GeometricVisualEffect.builder()
                                                                .withParticleBuilder(
                                                                                Particle.ITEM.builder().data(
                                                                                                ItemType.SLIME_BALL
                                                                                                                .createItemStack()))
                                                                .withShape(GeometricVisualEffect.Shape.Sphere.builder()
                                                                                .withOffset(new GeometricVisualEffect.Offset.FromPoint()
                                                                                                .withSpeed(1.2f))
                                                                                .withRadius(.25f).build())
                                                                .build())
                                                .addEffect(SoundEffect.builder()
                                                                .addSound(Sound.sound().source(Sound.Source.HOSTILE)
                                                                                .type(
                                                                                                org.bukkit.Sound.BLOCK_SLIME_BLOCK_FALL)
                                                                                .volume(3.0f)
                                                                                .build())
                                                                .addSoundWithDelay(Sound.sound()
                                                                                .source(Sound.Source.HOSTILE)
                                                                                .type(
                                                                                                org.bukkit.Sound.ENTITY_SLIME_ATTACK)
                                                                                .volume(3.0f)
                                                                                .build(), 2)
                                                                .addSoundWithDelay(Sound.sound()
                                                                                .source(Sound.Source.HOSTILE)
                                                                                .type(
                                                                                                org.bukkit.Sound.BLOCK_SLIME_BLOCK_PLACE)
                                                                                .volume(3.0f)
                                                                                .build(), 2)
                                                                .withPitchRange(0.8f, 1.2f)
                                                                .build())
                                                .addEffect(new KnockbackEffect(1))
                                                .addTrailEffect(VisualEffect.builder()
                                                                .addParticle(Particle.ITEM_SLIME.builder()
                                                                                .offset(0, 0, 0).extra(0))
                                                                .build())
                                                .withProjectileMaterial(Material.SLIME_BALL)
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

        public static NamespacedKey getSplitCountKey() {
                return splitCountKey;
        }

        public void register(Weapon weapon) {
                this.weapons.put(weapon.weaponID, weapon);
        }

        public Weapon getWeaponByID(NamespacedKey key) {
                return weapons.getOrDefault(key, defaultWeapon);
        }

        public Weapon getRandomWeapon() {
                Random random = new Random();
                return (Weapon) weapons.values().toArray()[random.nextInt(weapons.values().size())];
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
