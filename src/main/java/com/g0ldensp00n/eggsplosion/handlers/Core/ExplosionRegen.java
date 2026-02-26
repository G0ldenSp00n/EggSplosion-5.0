package com.g0ldensp00n.eggsplosion.handlers.Core;

import com.g0ldensp00n.eggsplosion.EggSplosion;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.WeaponRegistry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Tag;
import org.bukkit.ExplosionResult;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Collections;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ExplosionRegen implements Listener {

    private Location location;
    private BlockData blockData;
    private Material material;
    private Inventory inventory;
    private List<ExplosionRegen> blockQueue = new ArrayList<>();
    private List<ExplosionRegen> transparentQueue = new ArrayList<>();

    private EggSplosion plugin;

    public ExplosionRegen(EggSplosion plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private ExplosionRegen(Block block) {
        this.location = block.getLocation();
        this.material = block.getType();
        this.blockData = block.getBlockData();
        if (block.getType() == Material.CHEST) {
            Chest chest = (Chest) block.getState();
            inventory = Bukkit.createInventory(null, 27,
                    "" + chest.getLocation().getBlockX() + "" + chest.getLocation().getBlockZ());
            inventory.setContents(chest.getBlockInventory().getContents());
            chest.getBlockInventory().clear();
        }
    }

    public void update() {
        if (material == Material.AIR)
            return;

        if (material.hasGravity()) {
            Block blockBelow = location.clone().add(0, -1, 0).getBlock();
            if (!blockBelow.getType().isSolid()) {
                blockBelow.setType(Material.SANDSTONE);
            }
        }

        Block block = location.getBlock();

        block.setType(material, false);
        block.setBlockData(blockData, false);

        if (Tag.DOORS.isTagged(material) && blockData instanceof Door) {
            Door sourceDoor = (Door) blockData;

            if (sourceDoor.getHalf() == Bisected.Half.BOTTOM) {
                Block upperBlock = location.clone().add(0, 1, 0).getBlock();
                upperBlock.setType(material, false);

                Door upperDoor = (Door) upperBlock.getBlockData();
                upperDoor.setHalf(Bisected.Half.TOP);
                upperDoor.setHinge(sourceDoor.getHinge());
                upperDoor.setFacing(sourceDoor.getFacing());
                upperDoor.setOpen(sourceDoor.isOpen());
                upperDoor.setPowered(sourceDoor.isPowered());

                upperBlock.setBlockData(upperDoor, false);

            } else {
                Block lowerBlock = location.clone().add(0, -1, 0).getBlock();
                lowerBlock.setType(material, false);

                Door lowerDoor = (Door) lowerBlock.getBlockData();
                lowerDoor.setHalf(Bisected.Half.BOTTOM);
                lowerDoor.setHinge(sourceDoor.getHinge());
                lowerDoor.setFacing(sourceDoor.getFacing());
                lowerDoor.setOpen(sourceDoor.isOpen());
                lowerDoor.setPowered(sourceDoor.isPowered());

                lowerBlock.setBlockData(lowerDoor, false);
            }
        } else if (blockData instanceof Bisected && !Tag.TRAPDOORS.isTagged(material)
                && !Tag.STAIRS.isTagged(material)) {
            Bisected sourceBisected = (Bisected) blockData;

            if (sourceBisected.getHalf() == Bisected.Half.BOTTOM) {
                Block upperBlock = location.clone().add(0, 1, 0).getBlock();
                upperBlock.setType(material, false);

                Bisected upperData = (Bisected) upperBlock.getBlockData();
                upperData.setHalf(Bisected.Half.TOP);
                upperBlock.setBlockData(upperData, false);

            } else {
                Block lowerBlock = location.clone().add(0, -1, 0).getBlock();
                lowerBlock.setType(material, false);

                Bisected lowerData = (Bisected) lowerBlock.getBlockData();
                lowerData.setHalf(Bisected.Half.BOTTOM);
                lowerBlock.setBlockData(lowerData, false);
            }
        }

        if (block.getType() == Material.CHEST && block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            // Ensure inventory isn't null before setting
            if (this.inventory != null) {
                chest.getInventory().setContents(this.inventory.getContents());
            }
        }
    }

    @EventHandler
    public void explosionRegeneration(EntityExplodeEvent event) {
        ExplosionResult result = event.getExplosionResult();
        if (result == ExplosionResult.TRIGGER_BLOCK) {
            event.blockList().clear();
            return;
        }

        if (result == ExplosionResult.DESTROY || result == ExplosionResult.DESTROY_WITH_DECAY) {
            if (event.getEntity().getType() != EntityType.TNT) {
                event.setCancelled(true);
            }

            final int delay = 20 * 10;
            Set<ExplosionRegen> blockInfo = new HashSet<>();
            Set<ExplosionRegen> transparentBlocks = new HashSet<>();
            Set<Block> block_set = new HashSet<>();
            List<Block> blocks = event.blockList();
            Collections.sort(blocks, new Comparator<Block>() {
                @Override
                public int compare(Block blockA, Block blockB) {
                    if ((int) blockA.getLocation().getY() == blockB.getLocation().getY()) {
                        return 0;
                    }
                    return (((int) blockA.getLocation().getY()) > ((int) blockB.getLocation().getY())) ? -1 : 1;
                }
            });

            blocks.forEach(block -> {
                block_set.add(block);
                // Handle Gravity Blocks
                Block blockAbove = block.getLocation().add(0, 1, 0).getBlock();
                if (blockAbove.getType().hasGravity()) {
                    blockInfo.add(new ExplosionRegen(blockAbove));
                    blockAbove.setType(Material.AIR, false);

                    int blockAboveY = (int) blockAbove.getLocation().getY();
                    // Handle Stacks of Gravity Blocks
                    for (int blockY = 1; blockY <= (255 - blockAboveY); blockY++) {
                        Block blockStackCheck = blockAbove.getLocation().add(0, blockY, 0).getBlock();
                        if (blockStackCheck.getType().hasGravity()) {
                            blockInfo.add(new ExplosionRegen(blockStackCheck));
                            blockStackCheck.setType(Material.AIR, false);
                        } else {
                            break;
                        }
                    }
                }

                // Handle Solid Block
                if (block.getType().isSolid()) {
                    if (!block.getType().equals(Material.TNT) && !block.getType().equals(Material.BLUE_BANNER)
                            && !block.getType().equals(Material.RED_BANNER)) {
                        blockInfo.add(new ExplosionRegen(block));
                        block.setType(Material.AIR, false);
                    } else if (block.getType().equals(Material.TNT)) {
                        blockInfo.add(new ExplosionRegen(block));
                        block.setType(Material.AIR, false);
                        TNTPrimed primed = (TNTPrimed) block.getLocation().getWorld().spawnEntity(
                                block.getLocation(),
                                EntityType.TNT,
                                CreatureSpawnEvent.SpawnReason.EXPLOSION);
                        Random random = new Random();
                        primed.setFuseTicks(random.nextInt(1, 10));
                        if (event.getEntity() instanceof TNTPrimed) {
                            TNTPrimed sourceTNT = (TNTPrimed) event.getEntity();
                            primed.setSource(sourceTNT.getSource());
                        } else {
                            primed.setSource(event.getEntity());
                        }
                        block.getLocation().getWorld().spawnParticle(Particle.EXPLOSION, block.getLocation(), 0);
                    }
                    // Handle Non-Solid Blocks
                } else {
                    BlockData data = block.getBlockData();
                    if (data instanceof Bisected) {
                        Bisected bisected = (Bisected) data;
                        if (bisected.getHalf() == Bisected.Half.TOP) {
                            Block blockBelow = block.getLocation().add(0, -1, 0).getBlock();
                            if (!block_set.contains(blockBelow)) {
                                transparentBlocks.add(new ExplosionRegen(blockBelow));
                                blockBelow.setType(Material.AIR, false);
                            }
                        } else {
                            if (!block_set.contains(blockAbove)) {
                                transparentBlocks.add(new ExplosionRegen(block));
                            }
                        }
                    } else {
                        transparentBlocks.add(new ExplosionRegen(block));
                    }
                    block.setType(Material.AIR, false);
                }
            });

            // Add to Cleanup Queue
            blockQueue.addAll(blockInfo);
            List<ExplosionRegen> blockList = new ArrayList<>(blockInfo);
            Collections.sort(blockList, new Comparator<>() {
                @Override
                public int compare(ExplosionRegen blockA, ExplosionRegen blockB) {
                    if ((int) blockA.location.getY() == blockB.location.getY()) {
                        return 0;
                    }
                    return (((int) blockA.location.getY()) < ((int) blockB.location.getY())) ? -1 : 1;
                }
            });
            transparentQueue.addAll(transparentBlocks);
            List<ExplosionRegen> transparentBlockList = new ArrayList<ExplosionRegen>(transparentBlocks);
            Collections.sort(transparentBlockList, new Comparator<ExplosionRegen>() {
                @Override
                public int compare(ExplosionRegen blockA, ExplosionRegen blockB) {
                    if ((int) blockA.location.getY() == blockB.location.getY()) {
                        return 0;
                    }
                    return (((int) blockA.location.getY()) < ((int) blockB.location.getY())) ? -1 : 1;
                }
            });

            // Combine Solid and Non-Solid to a single list
            List<ExplosionRegen> allBlocks = new ArrayList<>(blockList);
            allBlocks.addAll(transparentBlockList);

            // Runnable to Delay Regen
            new BukkitRunnable() {
                Iterator<ExplosionRegen> allBlocksQueue = allBlocks.iterator();

                @Override
                public void run() {
                    for (int blocksPlaced = 0; blocksPlaced < 3; blocksPlaced++) {
                        if (allBlocksQueue.hasNext()) {
                            ExplosionRegen blockToPlace = allBlocksQueue.next();
                            blockToPlace.update();
                            allBlocksQueue.remove();
                        } else {
                            blockInfo.clear();
                            transparentBlocks.clear();
                            cancel();
                            break;
                        }
                    }
                }
            }.runTaskTimer(this.plugin, delay, (long) 0.01);
        }
    }

    public void repairAll() {
        Iterator<ExplosionRegen> blocksQueue = blockQueue.iterator();
        while (blocksQueue.hasNext()) {
            ExplosionRegen explosionRegenBlock = blocksQueue.next();
            explosionRegenBlock.update();
            blocksQueue.remove();
        }
        Iterator<ExplosionRegen> transparentsQueue = transparentQueue.iterator();
        while (blocksQueue.hasNext()) {
            ExplosionRegen explosionRegenTrans = transparentsQueue.next();
            explosionRegenTrans.update();
            transparentsQueue.remove();
        }
    }
}
