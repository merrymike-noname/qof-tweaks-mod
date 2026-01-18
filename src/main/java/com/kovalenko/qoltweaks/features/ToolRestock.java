package com.kovalenko.qoltweaks.features;

import com.kovalenko.qoltweaks.config.ModConfig;
import com.kovalenko.qoltweaks.core.Feature;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Comparator;
import java.util.Optional;

public class ToolRestock implements Feature {
    @Override
    public String getId() {
        return "toolRestock";
    }

    @Override
    public boolean isEnabled() {
        return ModConfig.TOOL_RESTOCK.get();
    }

    @Override
    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onToolBreak(PlayerDestroyItemEvent event) {
        if (!isEnabled()) return;
        ItemStack brokenTool = event.getOriginal();

        if (!(brokenTool.getItem() instanceof TieredItem || brokenTool.getItem() instanceof BowItem)) return;
        NonNullList<ItemStack> inventory = event.getPlayer().inventory.items;
        Class<?> toolClass = getToolClass(brokenTool.getItem());
        if (toolClass == null) return;

        Optional<ItemStack> replacement = inventory.stream()
                .filter(stack -> !stack.isEmpty() && toolClass.isAssignableFrom(stack.getItem().getClass()))
                .max(Comparator.comparingInt(this::getToolPriority));

        if (replacement.isPresent()) {
            ItemStack replacementTool = replacement.get();
            int inventoryIndex = inventory.indexOf(replacementTool);
            int hotbarSlot = event.getHand() == Hand.MAIN_HAND
                    ? event.getPlayer().inventory.selected
                    : -1;

            if (hotbarSlot >= 0) {
                event.getPlayer().inventory.setItem(hotbarSlot, replacementTool.copy());
                inventory.set(inventoryIndex, ItemStack.EMPTY);
            }
        }
    }

    private Class<?> getToolClass(Item item) {
        if (item instanceof PickaxeItem) return PickaxeItem.class;
        if (item instanceof AxeItem) return AxeItem.class;
        if (item instanceof SwordItem) return SwordItem.class;
        if (item instanceof HoeItem) return HoeItem.class;
        if (item instanceof BowItem) return BowItem.class;
        return null;
    }

    private int getToolPriority(ItemStack tool) {
        if (tool.getItem() instanceof TieredItem) {
            return getTierPriority(((TieredItem) tool.getItem()).getTier());
        } else if (tool.getItem() instanceof BowItem) {
            return 1;
        }
        return 0;
    }

    private int getTierPriority(IItemTier tier) {
        if (tier == ItemTier.NETHERITE) return 6;
        if (tier == ItemTier.DIAMOND) return 5;
        if (tier == ItemTier.IRON) return 4;
        if (tier == ItemTier.GOLD) return 3;
        if (tier == ItemTier.STONE) return 2;
        if (tier == ItemTier.WOOD) return 1;
        return 0;
    }

}