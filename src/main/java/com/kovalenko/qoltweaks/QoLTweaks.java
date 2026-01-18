package com.kovalenko.qoltweaks;

import com.kovalenko.qoltweaks.core.FeatureManager;
import com.kovalenko.qoltweaks.features.*;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import com.kovalenko.qoltweaks.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(QoLTweaks.MOD_ID)
public class QoLTweaks
{
    public static final String MOD_ID = "qoltweaks";
    private static final Logger LOGGER = LogManager.getLogger();

    public QoLTweaks() {

        ModLoadingContext.get().registerConfig(
                Type.COMMON,
                ModConfig.COMMON_SPEC
        );

        FeatureManager manager = new FeatureManager();

        manager.register(new DoubleDoorsOpener());
        manager.register(new TreeChop());
        manager.register(new VeinMine());
        manager.register(new ToolRestock());
        manager.register(new TorchPlacement());
    }
}
