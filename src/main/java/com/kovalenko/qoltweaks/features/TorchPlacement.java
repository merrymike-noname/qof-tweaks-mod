package com.kovalenko.qoltweaks.features;

import com.kovalenko.qoltweaks.config.ModConfig;
import com.kovalenko.qoltweaks.core.Feature;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TorchPlacement implements Feature {
    private static final int LIGHT_LEVEL = 4;

    @Override
    public String getId() {
        return "torchPlacement";
    }

    @Override
    public boolean isEnabled() {
        return ModConfig.TORCH_PLACEMENT.get();
    }

    @Override
    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!isEnabled()) return;
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide) return;

        PlayerEntity player = event.player;
        World world = player.level;

        BlockPos posBelow = player.blockPosition().below();
        posBelow = new BlockPos(posBelow.getX(), posBelow.getY() + 1, posBelow.getZ());
        BlockPos posTwoBelow = posBelow.below();

        if (!world.isEmptyBlock(posBelow)) return;
        if(world.getBlockState(posTwoBelow).isAir()) return;

        int lightLevel = world.getMaxLocalRawBrightness(posBelow);
        if (lightLevel > LIGHT_LEVEL) return;

        ItemStack torchStack = findTorchesInHotbar(player);
        if (torchStack.isEmpty()) return;

        world.setBlock(posBelow, Blocks.TORCH.defaultBlockState(), 3);
        torchStack.shrink(1);
    }

    private ItemStack findTorchesInHotbar(PlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.items.get(i);
            if (!stack.isEmpty() && stack.getItem() == Blocks.TORCH.asItem()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}