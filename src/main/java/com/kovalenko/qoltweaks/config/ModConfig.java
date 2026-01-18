package com.kovalenko.qoltweaks.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig {
    public static final ForgeConfigSpec COMMON_SPEC;

    public static ForgeConfigSpec.BooleanValue TOOL_RESTOCK;
    public static ForgeConfigSpec.BooleanValue TORCH_PLACEMENT;
    public static ForgeConfigSpec.BooleanValue VEIN_MINE;
    public static ForgeConfigSpec.BooleanValue TREE_CHOP;
    public static ForgeConfigSpec.BooleanValue DOUBLE_DOORS;
    public static ForgeConfigSpec.BooleanValue AUTO_REPLANT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("features");

        TOOL_RESTOCK = builder
                .comment("Automatically restock broken tools")
                .define("toolRestock", true);

        TORCH_PLACEMENT = builder
                .comment("Automatically place torches in dark areas")
                .define("torchPlacement", true);

        VEIN_MINE = builder
                .comment("Mine entire ore veins")
                .define("veinMine", true);

        TREE_CHOP = builder
                .comment("Chop down entire trees")
                .define("treeChop", true);

        DOUBLE_DOORS = builder
                .comment("Open double doors together")
                .define("doubleDoors", true);

        AUTO_REPLANT = builder
                .comment("Automatically replant crops")
                .define("autoReplant", true);

        builder.pop();

        COMMON_SPEC = builder.build();
    }
}