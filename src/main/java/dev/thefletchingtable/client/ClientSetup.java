package dev.thefletchingtable.client;

import dev.thefletchingtable.TheFletchingTableMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ClientSetup {

    @EventBusSubscriber(modid = TheFletchingTableMod.MOD_ID, value = Dist.CLIENT,
            bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {

        @SubscribeEvent
        public static void onRegisterScreens(RegisterMenuScreensEvent event) {
            event.register(TheFletchingTableMod.FLETCHING_MENU.get(), FletchingScreen::new);
        }
    }
}
