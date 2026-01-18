package com.kovalenko.qoltweaks.features;

import com.kovalenko.qoltweaks.config.ModConfig;
import com.kovalenko.qoltweaks.core.Feature;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class VeinMine implements Feature {
    @Override
    public String getId() {
        return "veinMine";
    }

    @Override
    public boolean isEnabled() {
        return ModConfig.VEIN_MINE.get();
    }

    @Override
    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        World world = (World) event.getWorld();
        if (world.isClientSide) return;
        if (!isEnabled()) return;

        PlayerEntity player = event.getPlayer();
        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);

        if (!state.is(Tags.Blocks.ORES)) return;
        ItemStack tool = player.getMainHandItem();
        if (!(tool.getItem() instanceof PickaxeItem)) return;
        if (!state.canHarvestBlock(world, pos, player)) return;

        Set<BlockPos> vein = collectVein(world, pos, state.getBlock());
        if (vein.size() <= 1) return;

        if (!hasEnoughDurability(tool, vein.size())) return;

        event.setCanceled(true);
        breakVein(world, player, tool, vein);
    }

    private Set<BlockPos> collectVein(World world, BlockPos start, Block originBlock) {

        Set<BlockPos> result = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();

        queue.add(start);
        result.add(start);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            for (BlockPos next : new BlockPos[]{
                    current.above(),
                    current.below(),
                    current.north(),
                    current.south(),
                    current.east(),
                    current.west()
            }) {
                if (result.contains(next)) continue;

                BlockState state = world.getBlockState(next);
                if (state.getBlock() == originBlock) {
                    result.add(next);
                    queue.add(next);
                }
            }
        }
        return result;
    }

    private boolean hasEnoughDurability(ItemStack tool, int blocks) {
        int unbreaking = EnchantmentHelper.getItemEnchantmentLevel(
                Enchantments.UNBREAKING, tool
        );
        float expectedDamage = blocks / (unbreaking + 1f);
        return tool.getDamageValue() + expectedDamage < tool.getMaxDamage();
    }

    private void breakVein(World world,
                           PlayerEntity player,
                           ItemStack tool,
                           Set<BlockPos> vein) {

        if (!(world instanceof net.minecraft.world.server.ServerWorld)) return;
        net.minecraft.world.server.ServerWorld serverWorld = (net.minecraft.world.server.ServerWorld) world;
        for (BlockPos pos : vein) {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            Block.dropResources(state, world, pos, null, player, tool);

            int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
            int silkTouch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool);

            int exp = block.getExpDrop(state, serverWorld, pos, fortune, silkTouch);
            if (exp > 0) {
                block.popExperience(serverWorld, pos, exp);
            }

            state.spawnAfterBreak(serverWorld, pos, tool);

            world.removeBlock(pos, false);
            tool.hurtAndBreak(1, player, p ->
                    p.broadcastBreakEvent(player.getUsedItemHand())
            );
        }


    }

}
