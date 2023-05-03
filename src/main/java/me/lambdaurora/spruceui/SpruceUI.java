package me.lambdaurora.spruceui;

import me.shedaniel.architectury.platform.forge.EventBuses;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SpruceUI.MODID)
public class SpruceUI {
    public static final String MODID = "spruceui";
    public static Logger LOGGER = LogManager.getLogger("SpruceUI-Forge");

    public SpruceUI() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(MODID, modEventBus);
        modEventBus.addListener(this::onInitializeClient);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onInitializeClient(FMLClientSetupEvent event) {
        LOGGER.info("SpruceUI is loaded!");
    }

}
