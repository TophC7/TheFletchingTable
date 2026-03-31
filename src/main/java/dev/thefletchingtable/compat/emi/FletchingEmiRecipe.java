package dev.thefletchingtable.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.thefletchingtable.TheFletchingTableMod;
import dev.thefletchingtable.compat.FletchingDisplay;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class FletchingEmiRecipe implements EmiRecipe {

    private final ResourceLocation id;
    private final EmiIngredient slot0;
    private final EmiIngredient slot1;
    private final EmiIngredient slot2;
    private final EmiStack result;

    public FletchingEmiRecipe(FletchingDisplay display, int index) {
        this.id = ResourceLocation.fromNamespaceAndPath(
                TheFletchingTableMod.MOD_ID, "fletching/" + index);
        this.slot0 = ingredientOf(display.slot0());
        this.slot1 = ingredientOf(display.slot1());
        this.slot2 = ingredientOf(display.slot2());
        this.result = EmiStack.of(display.result());
    }

    private static EmiIngredient ingredientOf(List<net.minecraft.world.item.ItemStack> stacks) {
        if (stacks.isEmpty()) return EmiStack.EMPTY;
        return EmiIngredient.of(stacks.stream().map(EmiStack::of).toList());
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return FletchingEmiPlugin.CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(slot0, slot1, slot2);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(result);
    }

    @Override
    public int getDisplayWidth() {
        return 120;
    }

    @Override
    public int getDisplayHeight() {
        return 18;
    }

    // RENDERING //

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(slot0, 0, 0);
        widgets.addSlot(slot1, 24, 0);
        widgets.addSlot(slot2, 48, 0);
        widgets.addSlot(result, 102, 0).recipeContext(this);
    }
}
