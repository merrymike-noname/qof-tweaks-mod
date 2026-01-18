package com.kovalenko.qoltweaks.features;

import com.kovalenko.qoltweaks.config.ModConfig;
import com.kovalenko.qoltweaks.core.Feature;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AutoReplant implements Feature {
    @Override
    public String getId() {
        return "autoReplant";
    }

    @Override
    public boolean isEnabled() {
        return ModConfig.AUTO_REPLANT.get();
    }

    @Override
    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!isEnabled()) return;

        World world = (World) event.getWorld();
        if (world.isClientSide) return;

        BlockPos pos = event.getPos();
        PlayerEntity player = event.getPlayer();
        BlockState state = world.getBlockState(pos);

        if (!(state.getBlock() instanceof CropsBlock)) return;

        CropsBlock crops = (CropsBlock) state.getBlock();
        boolean isMature = crops.isMaxAge(state);

        event.setCanceled(true);

        if (isMature) {
            crops.dropResources(state, world, pos, null, player, player.getMainHandItem());
        }

        BlockState replanted = crops.defaultBlockState();
        world.setBlock(pos, replanted, 3);
    }

}
