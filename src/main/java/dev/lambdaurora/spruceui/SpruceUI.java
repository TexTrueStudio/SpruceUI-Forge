package dev.lambdaurora.spruceui;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SpruceUI.MODID)
public class SpruceUI {
    public static final String MODID = "spruceui";
    public static Logger LOGGER = LogManager.getLogger("ObsidianUI");

    public SpruceUI() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::onInitializeClient);
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));

    }

    private void onInitializeClient(FMLClientSetupEvent event) {
        LOGGER.info("ObsidianUI is loaded!");
    }
}
