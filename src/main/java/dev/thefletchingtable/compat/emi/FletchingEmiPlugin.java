package dev.thefletchingtable.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.thefletchingtable.TheFletchingTableMod;
import dev.thefletchingtable.compat.FletchingDisplay;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@EmiEntrypoint
public class FletchingEmiPlugin implements EmiPlugin {

    public static final EmiStack WORKSTATION = EmiStack.of(Items.FLETCHING_TABLE);

    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(
                    TheFletchingTableMod.MOD_ID, "fletching"),
            WORKSTATION);

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, WORKSTATION);

        RecipeManager manager = registry.getRecipeManager();
        List<FletchingDisplay> displays = FletchingDisplay.allDisplays(manager);

        for (int i = 0; i < displays.size(); i++) {
            registry.addRecipe(new FletchingEmiRecipe(displays.get(i), i));
        }
    }
}
