package dev.thefletchingtable;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

@Mod(TheFletchingTableMod.MOD_ID)
public class TheFletchingTableMod {

    public static final String MOD_ID = "the_fletching_table";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Component TITLE =
            Component.translatable("container.the_fletching_table.fletching");

    // REGISTRY //

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, MOD_ID);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID);

    public static final Supplier<MenuType<FletchingMenu>> FLETCHING_MENU =
            MENU_TYPES.register("fletching",
                    () -> new MenuType<>(FletchingMenu::new, FeatureFlags.VANILLA_SET));

    public static final Supplier<RecipeType<FletchingRecipe>> FLETCHING_TYPE =
            RECIPE_TYPES.register("fletching",
                    () -> RecipeType.simple(
                            ResourceLocation.fromNamespaceAndPath(MOD_ID, "fletching")));

    public static final Supplier<RecipeSerializer<FletchingRecipe>> FLETCHING_SERIALIZER =
            RECIPE_SERIALIZERS.register("fletching", FletchingRecipe.Serializer::new);

    public TheFletchingTableMod(IEventBus modEventBus) {
        LOGGER.info("The Fletching Table loaded");

        MENU_TYPES.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }

    // BLOCK INTERACTION //

    @EventBusSubscriber(modid = MOD_ID)
    public static class InteractionEvents {

        @SubscribeEvent
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            if (!event.getLevel().getBlockState(event.getPos()).is(Blocks.FLETCHING_TABLE)) return;
            if (event.getEntity().isShiftKeyDown()) return;

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);

            if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer sp) {
                sp.openMenu(new SimpleMenuProvider(
                        (id, inv, p) -> new FletchingMenu(id, inv,
                                ContainerLevelAccess.create(sp.level(), event.getPos())),
                        TITLE
                ));
            }
        }
    }
}
