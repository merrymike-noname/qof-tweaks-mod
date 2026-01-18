package com.kovalenko.qoltweaks.features;

import com.kovalenko.qoltweaks.config.ModConfig;
import com.kovalenko.qoltweaks.core.Feature;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class TreeChop implements Feature {

    @Override
    public String getId() {
        return "treeChop";
    }

    @Override
    public boolean isEnabled() {
        return ModConfig.TREE_CHOP.get();
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

        if (!state.is(BlockTags.LOGS)) return;
        ItemStack tool = player.getMainHandItem();
        if (!(tool.getItem() instanceof AxeItem)) return;
        if (!isBottomLog(world, pos)) return;

        Set<BlockPos> logs = collectLogs(world, pos, state);
        if (logs.size() <= 1) return;

        if (!hasEnoughDurability(tool, logs.size())) return;

        event.setCanceled(true);
        breakLogs(world, player, tool, logs);
    }

    private boolean isBottomLog(World world, BlockPos pos) {
        BlockState below = world.getBlockState(pos.below());
        return !below.is(BlockTags.LOGS);
    }

    private Set<BlockPos> collectLogs(World world, BlockPos start, BlockState startState) {

        Set<BlockPos> result = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();

        Block originBlock = startState.getBlock();

        queue.add(start);
        result.add(start);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            for (BlockPos next : new BlockPos[]{
                    current.above(),
                    current.north(),
                    current.south(),
                    current.east(),
                    current.west()
            }) {
                if (result.contains(next)) continue;

                BlockState state = world.getBlockState(next);

                if (state.is(BlockTags.LOGS) && state.getBlock() == originBlock) {
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

    private void breakLogs(World world,
                           PlayerEntity player,
                           ItemStack tool,
                           Set<BlockPos> logs) {

        for (BlockPos logPos : logs) {
            BlockState logState = world.getBlockState(logPos);
            Block.dropResources(logState, world, logPos, null, player, tool);
            world.removeBlock(logPos, false);
            tool.hurtAndBreak(1, player, p ->
                    p.broadcastBreakEvent(player.getUsedItemHand())
            );
        }
    }
}