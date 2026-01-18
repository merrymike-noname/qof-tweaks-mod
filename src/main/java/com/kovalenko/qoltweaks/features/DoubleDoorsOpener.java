package com.kovalenko.qoltweaks.features;

import com.kovalenko.qoltweaks.config.ModConfig;
import com.kovalenko.qoltweaks.core.Feature;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DoubleDoorsOpener implements Feature {

    @Override
    public String getId() {
        return "doubleDoors";
    }

    @Override
    public boolean isEnabled() {
        return ModConfig.DOUBLE_DOORS.get();
    }

    @Override
    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onDoorRightClick(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        if (world.isClientSide) return;
        if (!isEnabled()) return;

        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);

        if (!(state.getBlock() instanceof DoorBlock)) return;

        if (state.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) return;

        DoorBlock door = (DoorBlock) state.getBlock();
        Direction facing = state.getValue(DoorBlock.FACING);
        DoorHingeSide hinge = state.getValue(DoorBlock.HINGE);
        boolean isOpen = state.getValue(DoorBlock.OPEN);

        Direction sideDir = (hinge == DoorHingeSide.LEFT)
                ? facing.getClockWise()
                : facing.getCounterClockWise();

        BlockPos otherPos = pos.relative(sideDir);
        BlockState otherState = world.getBlockState(otherPos);

        if (!(otherState.getBlock() instanceof DoorBlock)) return;
        if (otherState.getBlock() != door) return;

        if (otherState.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) return;
        if (otherState.getValue(DoorBlock.FACING) != facing) return;

        if (otherState.getValue(DoorBlock.HINGE) == hinge) return;

        world.setBlock(
                otherPos,
                otherState.setValue(DoorBlock.OPEN, !isOpen),
                10
        );
    }
}
